#include "WorkerManager.h"

using namespace MyBot;

WorkerManager::WorkerManager() 
{
	currentRepairWorker = nullptr;
}

WorkerManager & WorkerManager::Instance() 
{
	static WorkerManager instance;
	return instance;
}

void WorkerManager::update() 
{
	// 1�ʿ� 1���� �����Ѵ�
	if (BWAPI::Broodwar->getFrameCount() % 24 != 0) return;

	updateWorkerStatus();
	handleGasWorkers();
	handleIdleWorkers();
	handleMoveWorkers();
	handleCombatWorkers();
	handleRepairWorkers();
}

void WorkerManager::updateWorkerStatus() 
{
	// Drone �� �Ǽ��� ���� isConstructing = true ���·� �Ǽ���ұ��� �̵��� ��, 
	// ��� getBuildType() == none �� �Ǿ��ٰ�, isConstructing = true, isMorphing = true �� �� ��, �Ǽ��� �����Ѵ�

	// for each of our Workers
	for (auto & worker : workerData.getWorkers())
	{
		//if (workerData.getWorkerJob(worker) == WorkerData::Build && worker->getBuildType() == BWAPI::UnitTypes::None)
		//{
		//	std::cout << "construction worker " << worker->getID() << "buildtype BWAPI::UnitTypes::None " << std::endl;
		//}

		/*
		if (worker->isCarryingMinerals()) {
			std::cout << "mineral worker isCarryingMinerals " << worker->getID() 
				<< " isIdle: " << worker->isIdle()
				<< " isCompleted: " << worker->isCompleted()
				<< " isInterruptible: " << worker->isInterruptible()
				<< " target Name: " << worker->getTarget()->getType().getName()
				<< " job: " << workerData.getWorkerJob(worker)
				<< " exists " << worker->exists()
				<< " isConstructing " << worker->isConstructing()
				<< " isMorphing " << worker->isMorphing()
				<< " isMoving " << worker->isMoving()
				<< " isBeingConstructed " << worker->isBeingConstructed()
				<< " isStuck " << worker->isStuck()
				<< std::endl;
		}
		*/

		if (!worker->isCompleted())
		{
			continue;
		}

		// ���ӻ󿡼� worker�� isIdle ���°� �Ǿ����� (���� ź���߰ų�, ���� �ӹ��� ���� ���), WorkerData �� Idle �� ���� ��, handleGasWorkers, handleIdleWorkers ��� �� �ӹ��� �����Ѵ� 
		if ( worker->isIdle() )
		{
			/*
			if ((workerData.getWorkerJob(worker) == WorkerData::Build)
				|| (workerData.getWorkerJob(worker) == WorkerData::Move)
				|| (workerData.getWorkerJob(worker) == WorkerData::Scout)) {

				std::cout << "idle worker " << worker->getID()
					<< " job: " << workerData.getWorkerJob(worker)
					<< " exists " << worker->exists()
					<< " isConstructing " << worker->isConstructing()
					<< " isMorphing " << worker->isMorphing()
					<< " isMoving " << worker->isMoving()
					<< " isBeingConstructed " << worker->isBeingConstructed()
					<< " isStuck " << worker->isStuck()
					<< std::endl;
			}
			*/

			// workerData ���� Build / Move / Scout �� �ӹ������� ���, worker �� �� �ӹ� ���� ���� (�ӹ� �Ϸ� ��) �� �Ͻ������� isIdle ���°� �� �� �ִ� 
			if ((workerData.getWorkerJob(worker) != WorkerData::Build)
				&& (workerData.getWorkerJob(worker) != WorkerData::Move)
				&& (workerData.getWorkerJob(worker) != WorkerData::Scout))  
			{
				workerData.setWorkerJob(worker, WorkerData::Idle, nullptr);
			}
		}

		// if its job is gas
		if (workerData.getWorkerJob(worker) == WorkerData::Gas)
		{
			BWAPI::Unit refinery = workerData.getWorkerResource(worker);

			// if the refinery doesn't exist anymore (�ı��Ǿ��� ���)
			if (!refinery || !refinery->exists() ||	refinery->getHitPoints() <= 0)
			{
				workerData.setWorkerJob(worker, WorkerData::Idle, nullptr);
			}
		}

		// if its job is repair
		if (workerData.getWorkerJob(worker) == WorkerData::Repair)
		{
			BWAPI::Unit repairTargetUnit = workerData.getWorkerRepairUnit(worker);
						
			// ����� �ı��Ǿ��ų�, ������ �� ���� ���
			if (!repairTargetUnit || !repairTargetUnit->exists() || repairTargetUnit->getHitPoints() <= 0 || repairTargetUnit->getHitPoints() == repairTargetUnit->getType().maxHitPoints())
			{
				workerData.setWorkerJob(worker, WorkerData::Idle, nullptr);
			}
		}
	}
}


void WorkerManager::handleGasWorkers()
{
	// for each unit we have
	for (auto & unit : BWAPI::Broodwar->self()->getUnits())
	{
		// refinery �� �Ǽ� completed �Ǿ�����,
		if (unit->getType().isRefinery() && unit->isCompleted() )
		{
			// get the number of workers currently assigned to it
			int numAssigned = workerData.getNumAssignedWorkers(unit);

			// if it's less than we want it to be, fill 'er up
			// �̳׶� �ϲ��� ������ ���� �ϲ��� ������ 3~4���� ��� -> Config::Macro::WorkersPerRefinery ���� �����ؾ���
			for (int i = 0; i<(Config::Macro::WorkersPerRefinery - numAssigned); ++i)
			{
				BWAPI::Unit gasWorker = chooseGasWorkerFromMineralWorkers(unit);

				if (gasWorker)
				{
					//std::cout << "set gasWorker " << gasWorker->getID() << std::endl;
					workerData.setWorkerJob(gasWorker, WorkerData::Gas, unit);
				}
			}
		}
	}
}

void WorkerManager::handleIdleWorkers() 
{
	// for each of our workers
	for (auto & worker : workerData.getWorkers())
	{
		if (!worker) continue;

		// if worker's job is idle 
		if (workerData.getWorkerJob(worker) == WorkerData::Idle || workerData.getWorkerJob(worker) == WorkerData::Default )
		{
			// send it to the nearest mineral patch
			setMineralWorker(worker);
		}
	}
}

void WorkerManager::handleMoveWorkers()
{
	// for each of our workers
	for (auto & worker : workerData.getWorkers())
	{
		if (!worker) continue;

		// if it is a move worker
		if (workerData.getWorkerJob(worker) == WorkerData::Move)
		{
			WorkerMoveData data = workerData.getWorkerMoveData(worker);

			// �������� ������ ��� �̵� ����� �����Ѵ�
			if (worker->getPosition().getDistance(data.position) < 4) {
				setIdleWorker(worker);
			}
			else {
				CommandUtil::move(worker, data.position);
			}
		}
	}
}



// bad micro for combat workers
void WorkerManager::handleCombatWorkers()
{
	for (auto & worker : workerData.getWorkers())
	{
		if (!worker) continue;

		if (workerData.getWorkerJob(worker) == WorkerData::Combat)
		{
			BWAPI::Broodwar->drawCircleMap(worker->getPosition().x, worker->getPosition().y, 4, BWAPI::Colors::Yellow, true);
			BWAPI::Unit target = getClosestEnemyUnitFromWorker(worker);

			if (target)
			{
				CommandUtil::attackUnit(worker, target);
			}
		}
	}
}

BWAPI::Unit WorkerManager::getClosestEnemyUnitFromWorker(BWAPI::Unit worker)
{
	if (!worker) return nullptr;

	BWAPI::Unit closestUnit = nullptr;
	double closestDist = 10000;

	for (auto & unit : BWAPI::Broodwar->enemy()->getUnits())
	{
		double dist = unit->getDistance(worker);

		if ((dist < 400) && (!closestUnit || (dist < closestDist)))
		{
			closestUnit = unit;
			closestDist = dist;
		}
	}

	return closestUnit;
}

void WorkerManager::stopCombat()
{
	for (auto & worker : workerData.getWorkers())
	{
		if (!worker) continue;

		if (workerData.getWorkerJob(worker) == WorkerData::Combat)
		{
			setMineralWorker(worker);
		}
	}
}

void WorkerManager::handleRepairWorkers()
{
	if (BWAPI::Broodwar->self()->getRace() != BWAPI::Races::Terran)
	{
		return;
	}

	for (auto & unit : BWAPI::Broodwar->self()->getUnits())
	{
		// �ǹ��� ��� �ƹ��� �־ ������ ����. �ϲ� �Ѹ��� ������� ����
		if (unit->getType().isBuilding() && unit->isCompleted() == true && unit->getHitPoints() < unit->getType().maxHitPoints())
		{
			BWAPI::Unit repairWorker = chooseRepairWorkerClosestTo(unit->getPosition());
			setRepairWorker(repairWorker, unit);
			break;
		}
		// ��ī�� ���� (SCV, ������ũ, ���̾� ��)�� ��� ��ó�� SCV�� �ִ� ��� ����. �ϲ� �Ѹ��� ������� ����
		else if (unit->getType().isMechanical() && unit->isCompleted() == true && unit->getHitPoints() < unit->getType().maxHitPoints())
		{
			// SCV �� ���� ��󿡼� ����. ���� ���ָ� �����ϵ��� �Ѵ�
			if (unit->getType() != BWAPI::UnitTypes::Terran_SCV) {
				BWAPI::Unit repairWorker = chooseRepairWorkerClosestTo(unit->getPosition(), 10 * TILE_SIZE);
				setRepairWorker(repairWorker, unit);
				break;
			}
		}

	}
}

BWAPI::Unit WorkerManager::chooseRepairWorkerClosestTo(BWAPI::Position p, int maxRange)
{
	if (!p.isValid()) return nullptr;

    BWAPI::Unit closestWorker = nullptr;
    double closestDist = 100000000;

	if (currentRepairWorker != nullptr && currentRepairWorker->exists() && currentRepairWorker->getHitPoints() > 0)
    {
		return currentRepairWorker;
    }

    // for each of our workers
	for (auto & worker : workerData.getWorkers())
	{
		if (!worker)
		{
			continue;
		}

		if (worker->isCompleted() 
			&& (workerData.getWorkerJob(worker) == WorkerData::Minerals || workerData.getWorkerJob(worker) == WorkerData::Idle || workerData.getWorkerJob(worker) == WorkerData::Move))
		{
			double dist = worker->getDistance(p);

			if (!closestWorker || dist < closestDist)
            {
				closestWorker = worker;
                dist = closestDist;
            }
		}
	}

	if (currentRepairWorker == nullptr || currentRepairWorker->exists() == false || currentRepairWorker->getHitPoints() <= 0) {
		currentRepairWorker = closestWorker;
	}

	return closestWorker;
}

BWAPI::Unit WorkerManager::getScoutWorker()
{
    // for each of our workers
	for (auto & worker : workerData.getWorkers())
	{
		if (!worker)
		{
			continue;
		}
		// if it is a scout worker
        if (workerData.getWorkerJob(worker) == WorkerData::Scout) 
		{
			return worker;
		}
	}

    return nullptr;
}

// set a worker to mine minerals
void WorkerManager::setMineralWorker(BWAPI::Unit unit)
{
	if (!unit) return;

	// check if there is a mineral available to send the worker to
	BWAPI::Unit depot = getClosestResourceDepotFromWorker(unit);

	// if there is a valid ResourceDepot (Command Center, Nexus, Hatchery)
	if (depot)
	{
		// update workerData with the new job
		workerData.setWorkerJob(unit, WorkerData::Minerals, depot);
	}
}
BWAPI::Unit WorkerManager::getClosestMineralWorkerTo(BWAPI::Position target)
{
	BWAPI::Unit closestUnit = nullptr;
	double closestDist = 100000;
	
	for (auto & unit : BWAPI::Broodwar->self()->getUnits())
	{
		if (!unit)
		{
			continue;
		}

		if (unit->isCompleted()
			&& unit->getHitPoints() > 0
			&& unit->exists()
			&& unit->getType().isWorker()
			&& WorkerManager::Instance().isMineralWorker(unit))
		{
			double dist = unit->getDistance(target);
			if (!closestUnit || dist < closestDist)
			{
				closestUnit = unit;
				closestDist = dist;
			}
		}
	}

	return closestUnit;
}

BWAPI::Unit WorkerManager::getClosestResourceDepotFromWorker(BWAPI::Unit worker)
{
	if (!worker) return nullptr;

	BWAPI::Unit closestDepot = nullptr;
	double closestDistance = 0;

	for (auto & unit : BWAPI::Broodwar->self()->getUnits())
	{
		if (!unit) continue;
		
		// ���� �����, �ϲۼ��� �� ��������, �ϼ��� ResourceDepot (Ȥ�� Lair �� Hive�� �������� �ǹ�)�� ã�´�
		if (unit->getType().isResourceDepot()
			&& (unit->isCompleted() || unit->getType() == BWAPI::UnitTypes::Zerg_Lair || unit->getType() == BWAPI::UnitTypes::Zerg_Hive) )
		{
			double distance = unit->getDistance(worker);

			// �ϴ� ���� ResourceDepot �� �ϳ��� ���õǵ��� �Ѵ�
			if (!closestDepot )
			{
				closestDepot = unit;
				closestDistance = distance;
			}
			// �� ����� ResourceDepot �� �ְ�, �ϲ� ���� �� ���� �ʾҴٸ� �ű� ������ �Ѵ�
			else if (distance < closestDistance
				&& workerData.depotHasEnoughMineralWorkers(unit) == false) 
			{
				closestDepot = unit;
				closestDistance = distance;
			}
		}
	}

	return closestDepot;
}


// other managers that need workers call this when they're done with a unit
void WorkerManager::setIdleWorker(BWAPI::Unit unit)
{
	if (!unit) return;

	workerData.setWorkerJob(unit, WorkerData::Idle, nullptr);
}

// �ش� refinery �κ��� ���� �����, Mineral ĳ���ִ� �ϲ��� �����Ѵ�
BWAPI::Unit WorkerManager::chooseGasWorkerFromMineralWorkers(BWAPI::Unit refinery)
{
	if (!refinery) return nullptr;

	BWAPI::Unit closestWorker = nullptr;
	double closestDistance = 0;

	for (auto & unit : workerData.getWorkers())
	{
		if (!unit) continue;
		
		if (unit->isCompleted() && (workerData.getWorkerJob(unit) == WorkerData::Minerals))
		{
			double distance = unit->getDistance(refinery);
			if (!closestWorker || distance < closestDistance)
			{
				closestWorker = unit;
				closestDistance = distance;
			}
		}
	}

	return closestWorker;
}

void WorkerManager::setConstructionWorker(BWAPI::Unit worker, BWAPI::UnitType buildingType)
{
	if (!worker) return;

	workerData.setWorkerJob(worker, WorkerData::Build, buildingType);
}

BWAPI::Unit WorkerManager::chooseConstuctionWorkerClosestTo(BWAPI::UnitType buildingType, BWAPI::TilePosition buildingPosition, bool setJobAsConstructionWorker, int avoidWorkerID)
{
	// variables to hold the closest worker of each type to the building
	BWAPI::Unit closestMovingWorker = nullptr;
	BWAPI::Unit closestMiningWorker = nullptr;
	double closestMovingWorkerDistance = 0;
	double closestMiningWorkerDistance = 0;

	// look through each worker that had moved there first
	for (auto & unit : workerData.getWorkers())
	{
		if (!unit) continue;

		// worker �� 2�� �̻��̸�, avoidWorkerID �� ���Ѵ�
		if (workerData.getWorkers().size() >= 2 && avoidWorkerID != 0 && unit->getID() == avoidWorkerID) continue;

		// Move / Idle Worker
		if (unit->isCompleted() && (workerData.getWorkerJob(unit) == WorkerData::Move || workerData.getWorkerJob(unit) == WorkerData::Idle))
		{
			// if it is a new closest distance, set the pointer
			double distance = unit->getDistance(BWAPI::Position(buildingPosition));
			if (!closestMovingWorker || distance < closestMovingWorkerDistance)
			{
				if (BWTA::isConnected(unit->getTilePosition(), buildingPosition)) {
					closestMovingWorker = unit;
					closestMovingWorkerDistance = distance;
				}
			}
		}

		// Move / Idle Worker �� ������, �ٸ� Worker �߿��� �����Ѵ� 
		if (unit->isCompleted() && workerData.getWorkerJob(unit) != WorkerData::Move && workerData.getWorkerJob(unit) != WorkerData::Idle && workerData.getWorkerJob(unit) != WorkerData::Build)
		{
			// if it is a new closest distance, set the pointer
			double distance = unit->getDistance(BWAPI::Position(buildingPosition));
			if (!closestMiningWorker || distance < closestMiningWorkerDistance)
			{
				if (BWTA::isConnected(unit->getTilePosition(), buildingPosition)) {
					closestMiningWorker = unit;
					closestMiningWorkerDistance = distance;
				}
			}
		}
	}
	
	/*
	if (closestMiningWorker)
		std::cout << "closestMiningWorker " << closestMiningWorker->getID() << std::endl;
	if (closestMovingWorker)
		std::cout << "closestMovingWorker " << closestMovingWorker->getID() << std::endl;
	*/
	
	BWAPI::Unit chosenWorker = closestMovingWorker ? closestMovingWorker : closestMiningWorker;

	// if the worker exists (one may not have been found in rare cases)
	if (chosenWorker && setJobAsConstructionWorker)
	{
		workerData.setWorkerJob(chosenWorker, WorkerData::Build, buildingType);
	}

	return chosenWorker;
}

// sets a worker as a scout
void WorkerManager::setScoutWorker(BWAPI::Unit worker)
{
	if (!worker) return;

	workerData.setWorkerJob(worker, WorkerData::Scout, nullptr);
}

// get a worker which will move to a current location
BWAPI::Unit WorkerManager::chooseMoveWorkerClosestTo(BWAPI::Position p)
{
	// set up the pointer
	BWAPI::Unit closestWorker = nullptr;
	double closestDistance = 0;

	// for each worker we currently have
	for (auto & unit : workerData.getWorkers())
	{
		if (!unit) continue;

		// only consider it if it's a mineral worker
		if (unit->isCompleted() && (workerData.getWorkerJob(unit) == WorkerData::Minerals || workerData.getWorkerJob(unit) == WorkerData::Idle))
		{
			// if it is a new closest distance, set the pointer
			double distance = unit->getDistance(p);
			if (!closestWorker || distance < closestDistance)
			{
				closestWorker = unit;
				closestDistance = distance;
			}
		}
	}

	// return the worker
	return closestWorker;
}

// sets a worker to move to a given location
void WorkerManager::setMoveWorker(BWAPI::Unit worker, int mineralsNeeded, int gasNeeded, BWAPI::Position p)
{
	// set up the pointer
	BWAPI::Unit closestWorker = nullptr;
	double closestDistance = 0;

	// for each worker we currently have
	for (auto & unit : workerData.getWorkers())
	{
		if (!unit) continue;
		
		// only consider it if it's a mineral worker or idle worker
		if (unit->isCompleted() && (workerData.getWorkerJob(unit) == WorkerData::Minerals || workerData.getWorkerJob(unit) == WorkerData::Idle))
		{
			// if it is a new closest distance, set the pointer
			double distance = unit->getDistance(p);
			if (!closestWorker || distance < closestDistance)
			{
				closestWorker = unit;
				closestDistance = distance;
			}
		}
	}

	if (closestWorker)
	{
		workerData.setWorkerJob(closestWorker, WorkerData::Move, WorkerMoveData(mineralsNeeded, gasNeeded, p));
	}
	else
	{
		//BWAPI::Broodwar->printf("Error, no worker found");
	}
}

void WorkerManager::setCombatWorker(BWAPI::Unit worker)
{
	if (!worker) return;

	workerData.setWorkerJob(worker, WorkerData::Combat, nullptr);
}

void WorkerManager::setRepairWorker(BWAPI::Unit worker, BWAPI::Unit unitToRepair)
{
	workerData.setWorkerJob(worker, WorkerData::Repair, unitToRepair);
}

void WorkerManager::stopRepairing(BWAPI::Unit worker)
{
	workerData.setWorkerJob(worker, WorkerData::Idle, nullptr);
}


void WorkerManager::onUnitMorph(BWAPI::Unit unit)
{
	if (!unit) return;

	// if something morphs into a worker, add it
	if (unit->getType().isWorker() && unit->getPlayer() == BWAPI::Broodwar->self() && unit->getHitPoints() >= 0)
	{
		workerData.addWorker(unit);
	}

	// if something morphs into a building, it was a worker (Zerg Drone)
	if (unit->getType().isBuilding() && unit->getPlayer() == BWAPI::Broodwar->self() && unit->getPlayer()->getRace() == BWAPI::Races::Zerg)
	{
		// �ش� worker �� workerData ���� �����Ѵ�
		workerData.workerDestroyed(unit);
	}
}

void WorkerManager::onUnitShow(BWAPI::Unit unit)
{
	if (!unit) return;

	// add the depot if it exists
	if (unit->getType().isResourceDepot() && unit->getPlayer() == BWAPI::Broodwar->self())
	{
		workerData.addDepot(unit);
	}

	// add the worker
	if (unit->getType().isWorker() && unit->getPlayer() == BWAPI::Broodwar->self() && unit->getHitPoints() >= 0)
	{
		workerData.addWorker(unit);
	}

	if (unit->getType().isResourceDepot() && unit->getPlayer() == BWAPI::Broodwar->self())
	{
		rebalanceWorkers();
	}

}

// ���ϰ��ִ� resource depot �� ����� ���� mineral worker ���� �����Ǿ� �ִٸ�, idle ���·� �����
// idle worker ���� mineral job �� �ο��� ��, mineral worker �� ������ resource depot ���� �̵��ϰ� �ȴ�  
void WorkerManager::rebalanceWorkers()
{
	for (auto & worker : workerData.getWorkers())
	{
		if (!workerData.getWorkerJob(worker) == WorkerData::Minerals)
		{
			continue;
		}

		BWAPI::Unit depot = workerData.getWorkerDepot(worker);

		if (depot && workerData.depotHasEnoughMineralWorkers(depot))
		{
			workerData.setWorkerJob(worker, WorkerData::Idle, nullptr);
		}
		else if (!depot)
		{
			workerData.setWorkerJob(worker, WorkerData::Idle, nullptr);
		}
	}
}

void WorkerManager::onUnitDestroy(BWAPI::Unit unit) 
{
	if (!unit) return;

	if (unit->getType().isResourceDepot() && unit->getPlayer() == BWAPI::Broodwar->self())
	{
		workerData.removeDepot(unit);
	}

	if (unit->getType().isWorker() && unit->getPlayer() == BWAPI::Broodwar->self()) 
	{
		workerData.workerDestroyed(unit);
	}

	if (unit->getType() == BWAPI::UnitTypes::Resource_Mineral_Field)
	{
		rebalanceWorkers();
	}
}

bool WorkerManager::isMineralWorker(BWAPI::Unit worker)
{
	if (!worker) return false;

	return workerData.getWorkerJob(worker) == WorkerData::Minerals || workerData.getWorkerJob(worker) == WorkerData::Idle;
}

bool WorkerManager::isScoutWorker(BWAPI::Unit worker)
{
	if (!worker) return false;

	return (workerData.getWorkerJob(worker) == WorkerData::Scout);
}

bool WorkerManager::isConstructionWorker(BWAPI::Unit worker)
{
	if (!worker) return false;

	return (workerData.getWorkerJob(worker) == WorkerData::Build);
}

int WorkerManager::getNumMineralWorkers() 
{
	return workerData.getNumMineralWorkers();	
}

int WorkerManager::getNumIdleWorkers() 
{
	return workerData.getNumIdleWorkers();	
}

int WorkerManager::getNumGasWorkers() 
{
	return workerData.getNumGasWorkers();
}

WorkerData  WorkerManager::getWorkerData()
{
	return workerData;
}