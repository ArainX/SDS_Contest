#include "ConstructionManager.h"

using namespace MyBot;

ConstructionManager::ConstructionManager()
    : reservedMinerals(0)
    , reservedGas(0)
{

}

// add a new building to be constructed
void ConstructionManager::addConstructionTask(BWAPI::UnitType type, BWAPI::TilePosition desiredPosition)
{
	if (type == BWAPI::UnitTypes::None || type == BWAPI::UnitTypes::Unknown) {
		return;
	}
	if (desiredPosition == BWAPI::TilePositions::None || desiredPosition == BWAPI::TilePositions::Invalid || desiredPosition == BWAPI::TilePositions::Unknown) {
		return;
	}

	ConstructionTask b(type, desiredPosition);
	b.status = ConstructionStatus::Unassigned;

	// reserve resources
	reservedMinerals += type.mineralPrice();
	reservedGas += type.gasPrice();

	constructionQueue.push_back(b);
}

// ConstructionTask �ϳ��� �����Ѵ�
void ConstructionManager::cancelConstructionTask(BWAPI::UnitType type, BWAPI::TilePosition desiredPosition)
{
	reservedMinerals -= type.mineralPrice();
	reservedGas -= type.gasPrice();

	ConstructionTask b(type, desiredPosition);
    auto & it = std::find(constructionQueue.begin(), constructionQueue.end(), b);
    if (it != constructionQueue.end())
    {
		std::cout << std::endl << "Cancel Construction " << it->type.getName() << " at " << it->desiredPosition.x << "," << it->desiredPosition.y << std::endl;

		if (it->constructionWorker) {
			WorkerManager::Instance().setIdleWorker(it->constructionWorker);
		}
		if (it->finalPosition) {
			ConstructionPlaceFinder::Instance().freeTiles(it->finalPosition, it->type.tileWidth(), it->type.tileHeight());
		}
        constructionQueue.erase(it);
    }
}

// ConstructionTask �������� �����Ѵ�
// �Ǽ��� �����߾��� ConstructionTask �̱� ������ reservedMinerals, reservedGas �� �ǵ帮�� �ʴ´�
void ConstructionManager::removeCompletedConstructionTasks(const std::vector<ConstructionTask> & toRemove)
{
	for (auto & b : toRemove)
	{		
		auto & it = std::find(constructionQueue.begin(), constructionQueue.end(), b);

		if (it != constructionQueue.end())
		{
		    constructionQueue.erase(it);
		}
	}
}


// gets called every frame from GameCommander
void ConstructionManager::update()
{
	// 1�ʿ� 1���� �����Ѵ�
	//if (BWAPI::Broodwar->getFrameCount() % 24 != 0) return;

	// constructionQueue �� ����ִ� ConstructionTask ���� 
	// Unassigned -> Assigned (buildCommandGiven=false) -> Assigned (buildCommandGiven=true) -> UnderConstruction -> (Finished) �� ���� ��ȭ�ȴ�

	validateWorkersAndBuildings();
	assignWorkersToUnassignedBuildings();
	checkForStartedConstruction();
	constructAssignedBuildings();
	checkForDeadTerranBuilders();
	checkForCompletedBuildings();
	checkForDeadlockConstruction();
}

// STEP 1: DO BOOK KEEPING ON WORKERS WHICH MAY HAVE DIED
void ConstructionManager::validateWorkersAndBuildings()
{
	std::vector<ConstructionTask> toRemove;

	for (auto & b : constructionQueue)
    {
		if (b.status == ConstructionStatus::UnderConstruction)
		{
			// �Ǽ� ���� ���� (������ �޾Ƽ�) �Ǽ��Ϸ��� �ǹ��� �ı��� ���, constructionQueue ���� �����Ѵ�
			// �׷��� ������ (�Ƹ��� ������ ���������ִ�) ���� ��ġ�� �ٽ� �ǹ��� ������ �� ���̱� ����.
			if (b.buildingUnit == nullptr || !b.buildingUnit->getType().isBuilding() || b.buildingUnit->getHitPoints() <= 0 || !b.buildingUnit->exists())
			{
				std::cout << "Construction Failed case -> remove ConstructionTask " << b.type.getName() << std::endl;

				toRemove.push_back(b);

				if (b.constructionWorker) {
					WorkerManager::Instance().setIdleWorker(b.constructionWorker);
				}
			}
		}
    }

	removeCompletedConstructionTasks(toRemove);
}

// STEP 2: ASSIGN WORKERS TO BUILDINGS WITHOUT THEM
void ConstructionManager::assignWorkersToUnassignedBuildings()
{

	// for each building that doesn't have a builder, assign one
    for (ConstructionTask & b : constructionQueue)
    {
        if (b.status != ConstructionStatus::Unassigned)
        {
            continue;
        }

		//std::cout << "find build place near desiredPosition " << b.desiredPosition.x << "," << b.desiredPosition.y << std::endl;

		// �Ǽ� �ϲ��� Unassigned �� ���¿��� getBuildLocationNear �� �Ǽ��� ��ġ�� �ٽ� ���Ѵ�. -> Assigned 
		BWAPI::TilePosition testLocation = ConstructionPlaceFinder::Instance().getBuildLocationNear(b.type, b.desiredPosition);

		//if (Config::Debug::LogToConsole) std::cout << "ConstructionPlaceFinder Selected Location : " << testLocation.x << "," << testLocation.y << std::endl;

		if (testLocation == BWAPI::TilePositions::None || testLocation == BWAPI::TilePositions::Invalid || testLocation.isValid() == false) {
						
			// ���� �ǹ� ���� ��Ҹ� ���� ã�� �� ���� �� ����, 
			// desiredPosition ������ �ٸ� �ǹ�/���ֵ��� �ְ� �Ǿ��ų�, Pylon �� �ı��Ǿ��ų�, Creep �� ������ ����̰�,
			// ��κ� �ٸ� �ǹ�/���ֵ��� �ְԵ� ����̹Ƿ� ���� frame ���� �ٽ� ���� ���� Ž���Ѵ�
			continue;
		}

        // grab a worker unit from WorkerManager which is closest to this final position
		// �Ǽ��� ���ϴ� worker �� ��� construction worker �� ������ �� �ִ�. ������ �����Ǿ����� worker �� �ٽ� �������ϵ��� �Ѵ�
		BWAPI::Unit workerToAssign = WorkerManager::Instance().chooseConstuctionWorkerClosestTo(b.type, testLocation, true, b.lastConstructionWorkerID);
		
		//std::cout << "assignWorkersToUnassignedBuildings - chooseConstuctionWorkerClosest for " << b.type.getName().c_str() << " to worker near " << testLocation.x << "," << testLocation.y << std::endl;

        if (workerToAssign)
        {
			//std::cout << "set ConstuctionWorker " << workerToAssign->getID() << std::endl;

            b.constructionWorker = workerToAssign;
			
			b.finalPosition = testLocation;

			b.status = ConstructionStatus::Assigned;

			// reserve this building's space
			ConstructionPlaceFinder::Instance().reserveTiles(testLocation, b.type.tileWidth(), b.type.tileHeight());

			b.lastConstructionWorkerID = b.constructionWorker->getID();
        }
    }
}

// STEP 3: ISSUE CONSTRUCTION ORDERS TO ASSIGN BUILDINGS AS NEEDED
void ConstructionManager::constructAssignedBuildings()
{
    for (auto & b : constructionQueue)
    {
        if (b.status != ConstructionStatus::Assigned)
        {
            continue;
        }

		/*
		if (b.constructionWorker == nullptr) {
			std::cout << b.type.c_str() << " constructionWorker null" << std::endl;
		}
		else {
			std::cout << b.type.c_str() 
				<< " constructionWorker " << b.constructionWorker->getID()
				<< " exists " << b.constructionWorker->exists()
				<< " isIdle " << b.constructionWorker->isIdle()
				<< " isConstructing " << b.constructionWorker->isConstructing()
				<< " isMorphing " << b.constructionWorker->isMorphing() << std::endl;
		}
		*/

		// �ϲۿ��� build ����� ������ ������ isConstructing = false �̴�
		// ���� Ž������ ���� ���� ���ؼ��� build ����� ���� �� ����
		// �ϲۿ��� build ����� ������, isConstructing = true ���°� �Ǿ� �̵��� �ϴٰ�
		// build �� ������ �� ���� ��Ȳ�̶�� �ǴܵǸ� isConstructing = false ���°� �ȴ�
		// build �� ������ �� ������, �����佺 / �׶� ������ ��� �ϲ��� build �� �����ϰ�
		// ���� ���� �ǹ� �� Extractor �ǹ��� �ƴ� �ٸ� �ǹ��� ��� �ϲ��� exists = true, isConstructing = true, isMorphing = true �� �ǰ�, �ϲ� ID �� �ǹ� ID�� �ȴ�
		// ���� ���� �ǹ� �� Extractor �ǹ��� ��� �ϲ��� exists = false, isConstructing = true, isMorphing = true �� �� ��, �ϲ� ID �� �ǹ� ID�� �ȴ�. 
		//                  Extractor �ǹ� ���带 ���߿� ����ϸ�, ���ο� ID �� ���� �ϲ��� �ȴ�

		// �ϲ��� Assigned �� ��, UnderConstruction ���·� �Ǳ� ��, �� �ϲ��� �̵� �߿� �ϲ��� ���� ���, �ǹ��� Unassigned ���·� �ǵ��� �ϲ��� �ٽ� Assign �ϵ��� �Ѵ�		
		if (b.constructionWorker == nullptr || b.constructionWorker->exists() == false || b.constructionWorker->getHitPoints() <= 0)
		{
			// ���� ���� �ǹ� �� Extractor �ǹ��� ��� �ϲ��� exists = false ������ isConstructing = true �� �ǹǷ�, �ϲ��� ���� ��찡 �ƴϴ�
			if (b.type == BWAPI::UnitTypes::Zerg_Extractor && b.constructionWorker != nullptr && b.constructionWorker->isConstructing() == true) {
				continue;
			}

			//std::cout << "unassign " << b.type.getName().c_str() << " worker " << b.constructionWorker->getID() << ", because it is not exists" << std::endl;

			// Unassigned �� ���·� �ǵ�����
			WorkerManager::Instance().setIdleWorker(b.constructionWorker);

			// free the previous location in reserved
			ConstructionPlaceFinder::Instance().freeTiles(b.finalPosition, b.type.tileWidth(), b.type.tileHeight());

			b.constructionWorker = nullptr;

			b.buildCommandGiven = false;

			b.finalPosition = BWAPI::TilePositions::None;

			b.status = ConstructionStatus::Unassigned;
		}
		// if that worker is not currently constructing
		// �ϲ��� build command �� ������ isConstructing = true �� �ǰ� �Ǽ��� �ϱ����� �̵��ϴµ�,
		// isConstructing = false �� �Ǿ��ٴ� ����, build command �� ������ �� ���� ���ӿ��� �ش� �ӹ��� ��ҵǾ��ٴ� ���̴�
		else if (b.constructionWorker->isConstructing() == false)        
        {
            // if we haven't explored the build position, first we mush go there
			// �ѹ��� �Ȱ��� ������ build Ŀ�ǵ� ��ü�� ������ �� �����Ƿ�, �ϴ� �װ����� �̵��ϰ� �Ѵ�
            if (!isBuildingPositionExplored(b))
            {
                CommandUtil::move(b.constructionWorker,BWAPI::Position(b.finalPosition));
            }
			else if (b.buildCommandGiven == false)
            {
				//std::cout << b.type.c_str() << " build commanded to " << b.constructionWorker->getID() << ", buildCommandGiven true " << std::endl;

				// build command 
				b.constructionWorker->build(b.type, b.finalPosition);

				WorkerManager::Instance().setConstructionWorker(b.constructionWorker, b.type);

				// set the buildCommandGiven flag to true
				b.buildCommandGiven = true;

				b.lastBuildCommandGivenFrame = BWAPI::Broodwar->getFrameCount();

				b.lastConstructionWorkerID = b.constructionWorker->getID();
            }
			// if this is not the first time we've sent this guy to build this
			// �ϲۿ��� build command �� �־�����, �Ǽ������ϱ����� ���߿� �ڿ��� �̴��ϰ� �Ǿ��ų�, �ش� ��ҿ� �ٸ� ���ֵ��� �־ �Ǽ��� ���� ���ϰ� �ǰų�, Pylon �̳� Creep �� ������ ��� ���� �߻��� �� �ִ�
			// �� ���, �ش� �ϲ��� build command �� �����ϰ�, �ǹ� ���¸� Unassigned �� �ٲ㼭, �ٽ� �ǹ� ��ġ�� ���ϰ�, �ٸ� �ϲ��� �����ϴ� ������ ó���Ѵ�
			else
            {
				if (BWAPI::Broodwar->getFrameCount() - b.lastBuildCommandGivenFrame > 24) {

					//std::cout << b.type.c_str()
					//	<< " constructionWorker " << b.constructionWorker->getID()
					//	<< " buildCommandGiven " << b.buildCommandGiven
					//	<< " lastBuildCommandGivenFrame " << b.lastBuildCommandGivenFrame
					//	<< " lastConstructionWorkerID " << b.lastConstructionWorkerID
					//	<< " exists " << b.constructionWorker->exists()
					//	<< " isIdle " << b.constructionWorker->isIdle()
					//	<< " isConstructing " << b.constructionWorker->isConstructing()
					//	<< " isMorphing " << b.constructionWorker->isMorphing() << std::endl;

					//std::cout << b.type.c_str() << "(" << b.finalPosition.x << "," << b.finalPosition.y << ") buildCommandGiven -> but now Unassigned " << std::endl;

					// tell worker manager the unit we had is not needed now, since we might not be able
					// to get a valid location soon enough
					WorkerManager::Instance().setIdleWorker(b.constructionWorker);

					// free the previous location in reserved
					ConstructionPlaceFinder::Instance().freeTiles(b.finalPosition, b.type.tileWidth(), b.type.tileHeight());

					// nullify its current builder unit
					b.constructionWorker = nullptr;

					// nullify its current builder unit
					b.finalPosition = BWAPI::TilePositions::None;

					// reset the build command given flag
					b.buildCommandGiven = false;

					// add the building back to be assigned
					b.status = ConstructionStatus::Unassigned;
				}
			}
        }
    }
}

// STEP 4: UPDATE DATA STRUCTURES FOR BUILDINGS STARTING CONSTRUCTION
void ConstructionManager::checkForStartedConstruction()
{
    // for each building unit which is being constructed
    for (auto & buildingThatStartedConstruction : BWAPI::Broodwar->self()->getUnits())
    {
        // filter out units which aren't buildings under construction
        if (!buildingThatStartedConstruction->getType().isBuilding() || !buildingThatStartedConstruction->isBeingConstructed())
        {
            continue;
        }

		//std::cout << "buildingThatStartedConstruction " << buildingThatStartedConstruction->getType().getName().c_str() << " isBeingConstructed at " << buildingThatStartedConstruction->getTilePosition().x << "," << buildingThatStartedConstruction->getTilePosition().y << std::endl;

        // check all our building status objects to see if we have a match and if we do, update it
        for (auto & b : constructionQueue)
        {
            if (b.status != ConstructionStatus::Assigned)
            {
                continue;
            }
        
            // check if the positions match.  Worker just started construction.
            if (b.finalPosition == buildingThatStartedConstruction->getTilePosition())
            {
				//std::cout << "construction " << b.type.getName().c_str() << " started at " << b.finalPosition.x << "," << b.finalPosition.y << std::endl;

                // the resources should now be spent, so unreserve them
                reservedMinerals -= buildingThatStartedConstruction->getType().mineralPrice();
                reservedGas      -= buildingThatStartedConstruction->getType().gasPrice();

                // flag it as started and set the buildingUnit
                b.underConstruction = true;

                b.buildingUnit = buildingThatStartedConstruction;

                // if we are zerg, make the buildingUnit nullptr since it's morphed or destroyed
				// Extractor �� ��� destroyed �ǰ�, �׿� �ǹ��� ��� morphed �ȴ�
                if (BWAPI::Broodwar->self()->getRace() == BWAPI::Races::Zerg)
                {
                    b.constructionWorker = nullptr;
                }
				// if we are protoss, give the worker back to worker manager
				else if (BWAPI::Broodwar->self()->getRace() == BWAPI::Races::Protoss)
                {
                    WorkerManager::Instance().setIdleWorker(b.constructionWorker);

                    b.constructionWorker = nullptr;
                }

                // free this space
                ConstructionPlaceFinder::Instance().freeTiles(b.finalPosition,b.type.tileWidth(),b.type.tileHeight());

				// put it in the under construction vector
				b.status = ConstructionStatus::UnderConstruction;

				// only one building will match
                break;
            }
        }
    }
}

// STEP 5: IF WE ARE TERRAN, THIS MATTERS
// �׶��� �Ǽ��� ������ ��, �Ǽ� ���߿� �ϲ��� ���� �� �ִ�. �� ���, �ǹ��� ���� �ٽ� �ٸ� SCV�� �Ҵ��Ѵ�
// �����, �����佺 / ���״� �Ǽ��� �����ϸ� �ϲ� �����͸� nullptr �� ����� ������ (constructionWorker = nullptr) �Ǽ� ���߿� ���� �ϲ��� �Ű澵 �ʿ� ���� 
void ConstructionManager::checkForDeadTerranBuilders()
{
	if (BWAPI::Broodwar->self()->getRace() == BWAPI::Races::Terran) {

		if (BWAPI::Broodwar->self()->completedUnitCount(BWAPI::UnitTypes::Terran_SCV) <= 0) return;

		// for each of our buildings under construction
		for (auto & b : constructionQueue)
		{
			// if a terran building whose worker died mid construction, 
			// send the right click command to the buildingUnit to resume construction			
			if (b.status == ConstructionStatus::UnderConstruction) {

				if (b.buildingUnit->isCompleted()) continue;

				if (b.constructionWorker == nullptr || b.constructionWorker->exists() == false || b.constructionWorker->getHitPoints() <= 0 ) {

					//std::cout << "checkForDeadTerranBuilders - chooseConstuctionWorkerClosest for " << b.type.getName().c_str() << " to worker near " << b.finalPosition.x << "," << b.finalPosition.y << std::endl;

					// grab a worker unit from WorkerManager which is closest to this final position
					BWAPI::Unit workerToAssign = WorkerManager::Instance().chooseConstuctionWorkerClosestTo(b.type, b.finalPosition, true, b.lastConstructionWorkerID);

					if (workerToAssign)
					{
						//std::cout << "set ConstuctionWorker " << workerToAssign->getID() << std::endl;

						b.constructionWorker = workerToAssign;

						//b.status �� ��� UnderConstruction �� �д�. Assigned �� �ٲٸ�, �ᱹ Unassigned �� �Ǿ ���� ���� �Ǳ� �����̴�
						//b.status = ConstructionStatus::Assigned;

						CommandUtil::rightClick(b.constructionWorker, b.buildingUnit);

						b.buildCommandGiven = true;

						b.lastBuildCommandGivenFrame = BWAPI::Broodwar->getFrameCount();

						b.lastConstructionWorkerID = b.constructionWorker->getID();
					}
				}
			}
		}
	}

}

// STEP 6: CHECK FOR COMPLETED BUILDINGS
void ConstructionManager::checkForCompletedBuildings()
{
    std::vector<ConstructionTask> toRemove;

    // for each of our buildings under construction
    for (auto & b : constructionQueue)
    {
        if (b.status != ConstructionStatus::UnderConstruction)
        {
            continue;       
        }

        // if the unit has completed
        if (b.buildingUnit->isCompleted())
        {
			//std::cout << "construction " << b.type.getName().c_str() << " completed at " << b.finalPosition.x << "," << b.finalPosition.y << std::endl;
			
			// if we are terran, give the worker back to worker manager
            if (BWAPI::Broodwar->self()->getRace() == BWAPI::Races::Terran)
            {
                WorkerManager::Instance().setIdleWorker(b.constructionWorker);
            }

            // remove this unit from the under construction vector
            toRemove.push_back(b);
        }
    }

    removeCompletedConstructionTasks(toRemove);
}


void ConstructionManager::checkForDeadlockConstruction()
{
	std::vector<ConstructionTask> toCancel;
	for (auto & b : constructionQueue)
	{
		if (b.status != ConstructionStatus::UnderConstruction)
		{
			// BuildManager�� �Ǵ������� Construction ���������� �������� ConstructionManager�� ConstructionQueue �� ���µ�, 
			// ���� �ǹ��� �ı��Ǽ� Construction�� ������ �� ���� �Ǿ��ų�,
			// �ϲ��� �� ����ϴ� �� ���ӻ�Ȳ�� �ٲ�, ��� ConstructionQueue �� �����ְ� �Ǵ� dead lock ��Ȳ�� �ȴ� 
			// ���� �ǹ��� BuildQueue�� �߰��س�����, �ش� ConstructionQueueItem �� �������� ���������� �Ǵ��ؾ� �Ѵ�
			BWAPI::UnitType unitType = b.type;
			BWAPI::UnitType producerType = b.type.whatBuilds().first;
			const std::map< BWAPI::UnitType, int >& requiredUnits = unitType.requiredUnits();
			BWTA::Region* desiredPositionRegion = BWTA::getRegion(b.desiredPosition);

			bool isDeadlockCase = false;

			// �ǹ��� �����ϴ� �����̳�, ������ �����ϴ� �ǹ��� �������� �ʰ�, �Ǽ� ���������� ������ dead lock
			if (BuildManager::Instance().isProducerWillExist(producerType) == false) {
				isDeadlockCase = true;
			}

			// Refinery �ǹ��� ���, �ǹ� ���� ��Ҹ� ã�� �� ���� �Ǿ��ų�, �ǹ� ���� �� �����Ŷ�� �Ǵ��ߴµ� �̹� Refinery �� �������ִ� ���, dead lock 
			if (!isDeadlockCase && unitType == InformationManager::Instance().getRefineryBuildingType())
			{
				bool hasAvailableGeyser = true;

				BWAPI::TilePosition testLocation;
				if (b.finalPosition != BWAPI::TilePositions::None && b.finalPosition != BWAPI::TilePositions::Invalid && b.finalPosition.isValid()) {
					testLocation = b.finalPosition;
				}
				else {
					testLocation = ConstructionPlaceFinder::Instance().getBuildLocationNear(b.type, b.desiredPosition);
				}

				// Refinery �� �������� ��Ҹ� ã�� �� ������ dead lock
				if (testLocation == BWAPI::TilePositions::None || testLocation == BWAPI::TilePositions::Invalid || testLocation.isValid() == false) {
					std::cout << "Construction Dead lock case -> Cann't find place to construct " << b.type.getName() << std::endl;
					hasAvailableGeyser = false;
				}
				else {
					// Refinery �� �������� ��ҿ� Refinery �� �̹� �Ǽ��Ǿ� �ִٸ� dead lock 
					BWAPI::Unitset uot = BWAPI::Broodwar->getUnitsOnTile(testLocation);
					for (auto & u : uot) {
						if (u->getType().isRefinery() && u->exists() ) {
							hasAvailableGeyser = false;
							break;
						}
					}
					if (hasAvailableGeyser == false) {
						std::cout << "Construction Dead lock case -> Refinery Building was built already at " << testLocation.x << ", " << testLocation.y << std::endl;
					}
				}

				if (hasAvailableGeyser == false) {
					isDeadlockCase = true;
				}
			}

			// ������� Ȥ�� �������, �Ǽ� ��Ұ� �Ʊ� ���� Region �� �ƴϰ�, ������ ������ Region �� �Ǿ����� �Ϲ������δ� ���������� dead lock �� �ȴ� 
			// (����ĳ�� �����̰ų�, ���� ���� Region ��ó���� �׶� �ǹ� �Ǽ��ϴ� ��쿡�� ������������..)
			if (!isDeadlockCase
				&& InformationManager::Instance().getOccupiedRegions(InformationManager::Instance().selfPlayer).find(desiredPositionRegion) == InformationManager::Instance().getOccupiedRegions(InformationManager::Instance().selfPlayer).end()
				&& InformationManager::Instance().getOccupiedRegions(InformationManager::Instance().enemyPlayer).find(desiredPositionRegion) != InformationManager::Instance().getOccupiedRegions(InformationManager::Instance().enemyPlayer).end())
			{
				isDeadlockCase = true;
			}

			// ���� �ǹ�/������ �ִµ� 
			if (!isDeadlockCase && requiredUnits.size() > 0)
			{
				for (auto & u : requiredUnits)
				{
					BWAPI::UnitType requiredUnitType = u.first;

					if (requiredUnitType != BWAPI::UnitTypes::None) {

						// ���� �ǹ� / ������ �������� �ʰ�, ���� �������� �ʰ�
						if (BWAPI::Broodwar->self()->completedUnitCount(requiredUnitType) == 0
							&& BWAPI::Broodwar->self()->incompleteUnitCount(requiredUnitType) == 0)
						{
							// ���� �ǹ��� �Ǽ� ���������� ������ dead lock
							if (requiredUnitType.isBuilding())
							{
								if (ConstructionManager::Instance().getConstructionQueueItemCount(requiredUnitType) == 0) {
									isDeadlockCase = true;
								}
							}
						}
					}
				}
			}

			if (isDeadlockCase) {
				toCancel.push_back(b);
			}
		}
	}

	for (auto & i : toCancel)
	{
		cancelConstructionTask(i.type, i.desiredPosition);
	}
}

// COMPLETED
bool ConstructionManager::isEvolvedBuilding(BWAPI::UnitType type) 
{
    if (type == BWAPI::UnitTypes::Zerg_Sunken_Colony ||
        type == BWAPI::UnitTypes::Zerg_Spore_Colony ||
        type == BWAPI::UnitTypes::Zerg_Lair ||
        type == BWAPI::UnitTypes::Zerg_Hive ||
        type == BWAPI::UnitTypes::Zerg_Greater_Spire)
    {
        return true;
    }

    return false;
}

bool ConstructionManager::isBuildingPositionExplored(const ConstructionTask & b) const
{
    BWAPI::TilePosition tile = b.finalPosition;

    // for each tile where the building will be built
    for (int x=0; x<b.type.tileWidth(); ++x)
    {
        for (int y=0; y<b.type.tileHeight(); ++y)
        {
            if (!BWAPI::Broodwar->isExplored(tile.x + x,tile.y + y))
            {
                return false;
            }
        }
    }

    return true;
}

int ConstructionManager::getReservedMinerals() 
{
    return reservedMinerals;
}

int ConstructionManager::getReservedGas() 
{
    return reservedGas;
}


ConstructionManager & ConstructionManager::Instance()
{
    static ConstructionManager instance;
    return instance;
}

std::vector<BWAPI::UnitType> ConstructionManager::buildingsQueued()
{
    std::vector<BWAPI::UnitType> buildingsQueued;

    for (const auto & b : constructionQueue)
    {
        if (b.status == ConstructionStatus::Unassigned || b.status == ConstructionStatus::Assigned)
        {
            buildingsQueued.push_back(b.type);
        }
    }

    return buildingsQueued;
}

// constructionQueue�� �ش� type �� Item �� �����ϴ��� ī��Ʈ�Ѵ�. queryTilePosition �� �Է��� ���, ��ġ�� �Ÿ������� ����Ѵ�
int ConstructionManager::getConstructionQueueItemCount(BWAPI::UnitType queryType, BWAPI::TilePosition queryTilePosition)
{
	// queryTilePosition �� �Է��� ���, �Ÿ��� maxRange. Ÿ�ϴ���
	int maxRange = 16;

	const BWAPI::Point<int, 32> queryTilePositionPoint(queryTilePosition.x, queryTilePosition.y);

	int count = 0;
	for (auto & b : constructionQueue)
	{
		if (b.type == queryType)
		{
			if (queryType.isBuilding() && queryTilePosition != BWAPI::TilePositions::None)
			{
				if (queryTilePositionPoint.getDistance(b.desiredPosition) <= maxRange) {
					count++;
				}
			}
			else {
				count++;
			}
		}
	}

	return count;
}

std::vector<ConstructionTask> * ConstructionManager::getConstructionQueue()
{
	return & constructionQueue;
}
