#include "WorkerManager.h"

using namespace MyBot;

WorkerManager & WorkerManager::Instance()
{
	static WorkerManager instance;
	return instance;
}

WorkerManager::WorkerManager()
{
	for (auto & unit : BWAPI::Broodwar->getAllUnits())
	{
		if ((unit->getType() == BWAPI::UnitTypes::Resource_Mineral_Field))
		{
			workerCountOnMineral[unit] = 0;
		}
	}
}


void WorkerManager::update() 
{
	// ������ Worker �� ���ؼ�
	// ���� ����� �Ʊ� ResourceDepot ��ó��, ���� ����� Mineral �� ä���ϵ��� �ϵ� (�Ÿ� ��� �ʿ�)
	// Worker ���� ���� Mineral �� �л�ǵ��� �Ѵ� (������ Mineral �� �Ҵ�� worker ���� ���ڸ� ���� / �ֽ�ȭ �ؾ� �Ѵ�)

	// worker �� ���� ���� ���� idle ���°� ������, ���� �����ϴ� ���߿��� ��� idle ���°� �ȴ�
	for (auto & unit : BWAPI::Broodwar->self()->getUnits()){

		if (!unit) continue;
		
		if (unit->getType().isWorker()) {

			// unit �� idle �����̰�, ź���� �����̸� 
			if (unit->isIdle() && unit->isCompleted())
			{
				std::cout << unit->getType().getName() << " " << unit->getID() << " is idle" << std::endl;

				// unit ���� ������ Mineral �� ã��, �� Mineral �� Right Click �� �Ѵ�
				BWAPI::Unit bestMineral = getBestMineralTo(unit);

				if (bestMineral) {
					std::cout << "bestMineral from " << unit->getType().getName() << " " << unit->getID()
						<< " is " << bestMineral->getType().getName() << " " << bestMineral->getID() << " at " << bestMineral->getTilePosition().x << "," << bestMineral->getTilePosition().y << std::endl;

					unit->gather(bestMineral);

					// unit �� Mineral �� assign ������ ������Ʈ�Ѵ�
					workerMineralAssignment[unit] = bestMineral;
					// Mineral �� assigned unit ���ڸ� ������Ʈ�Ѵ�
					increaseWorkerCountOnMineral(bestMineral, 1);
				}
			}
		}
	}

	// Mineral �� assigned unit ���ڸ� ȭ�鿡 ǥ��
	for (auto & i : workerMineralAssignment) {
		if (i.first != nullptr && i.second != nullptr) {
			BWAPI::Unit mineral = i.second;
			if (workerCountOnMineral.find(mineral) != workerCountOnMineral.end()) {
				BWAPI::Broodwar->drawTextMap(mineral->getPosition().x, mineral->getPosition().y + 12, "worker: %d", workerCountOnMineral[mineral]);
			}
		}
	}
}

BWAPI::Unit WorkerManager::getBestMineralTo(BWAPI::Unit worker)
{
	if (!worker) return nullptr;

	// worker���κ��� ���� ����� BaseLocation�� ã�´�
	BWTA::BaseLocation * closestBaseLocation = nullptr;
	// 128 * 128 Ÿ�ϻ������� �ʿ��� ���� �� �Ÿ��� sqrt(128 * 32  * 128 * 32 + 128 * 32 * 128 * 32) = 5792.6 point 
	double closestDistance = 1000000000;

	for (auto & baseLocation : BWTA::getBaseLocations()){

		if (!baseLocation) continue;

		double distance = worker->getDistance(baseLocation->getPosition());

		if (distance < closestDistance)
		{
			closestBaseLocation = baseLocation;
			closestDistance = distance;
		}
	}

	if (!closestBaseLocation) {
		return nullptr;
	}
	std::cout << "closestBaseLocation from " << worker->getType().getName() << " " << worker->getID()
		<< " is " << closestBaseLocation->getTilePosition().x << "," << closestBaseLocation->getTilePosition().y << std::endl;

	// �ش� BaseLocation �� Mineral �� �߿��� worker �� ���� ���� �����Ǿ��ִ� ��, ���߿����� BaseLocation ���κ��� ���� ����� ���� ã�´�
	BWAPI::Unit bestMineral = nullptr;
	double bestDistance = 1000000000;
	int bestNumAssigned = 1000000000;

	//BaseLocation->getMinerals() -> ��ο� ������ ������, null �� ����
	//BaseLocation->getStaticMinerals() -> ��ο� ������ ������, BWAPI::UnitTypes::Unknown �� ����
	for (auto & mineral : closestBaseLocation->getMinerals()){
		if (!mineral) continue;

		// �ش� Mineral �� ������ worker ����
		int numAssigned = workerCountOnMineral.find(mineral) == workerCountOnMineral.end() ? 0 : workerCountOnMineral[mineral];
		// �ش� Mineral �� BaseLocation ���� �Ÿ�
		double dist = mineral->getDistance(closestBaseLocation->getPosition());

		if (numAssigned < bestNumAssigned)
		{
			bestMineral = mineral;
			bestDistance = dist;
			bestNumAssigned = numAssigned;
		}
		else if (numAssigned == bestNumAssigned)
		{
			if (dist < bestDistance)
			{
				bestMineral = mineral;
				bestDistance = dist;
				bestNumAssigned = numAssigned;
			}
		}
	}

	return bestMineral;
}

void WorkerManager::increaseWorkerCountOnMineral(BWAPI::Unit mineral, int num)
{
	// Mineral �� assign �� worker ���ڸ� �����Ѵ�
	if (workerCountOnMineral.find(mineral) == workerCountOnMineral.end())
	{
		workerCountOnMineral[mineral] = num;
	}
	else
	{
		workerCountOnMineral[mineral] = workerCountOnMineral[mineral] + num;
	}
}

void WorkerManager::onUnitDestroy(BWAPI::Unit unit)
{
	if (!unit) return;

	if (unit->getType().isWorker() && unit->getPlayer() == BWAPI::Broodwar->self()) 
	{
		// �ش� �ϲ۰� Mineral �� assign ������ �����Ѵ�
		std::cout << "removeWorker " << unit->getID() << " from Mineral Worker " << std::endl;
		increaseWorkerCountOnMineral(workerMineralAssignment[unit], -1);
		workerMineralAssignment.erase(unit);
	}
}
void WorkerManager::onUnitMorph(BWAPI::Unit unit)
{
	if (!unit) return;

	// ���� ���� �ϲ��� �ǹ��� morph �� ���
	if (unit->getPlayer()->getRace() == BWAPI::Races::Zerg && unit->getPlayer() == BWAPI::Broodwar->self() && unit->getType().isBuilding())
	{
		// �ش� �ϲ۰� Mineral �� assign ������ �����Ѵ�
		std::cout << "removeWorker " << unit->getID() << " from Mineral Worker " << std::endl;				
		increaseWorkerCountOnMineral(workerMineralAssignment[unit], -1);
		workerMineralAssignment.erase(unit);
	}
}

