#include "BuildManager.h"

using namespace MyBot;

BuildManager & BuildManager::Instance()
{
	static BuildManager instance;
	return instance;
}

BuildManager::BuildManager() 
{
}

void BuildManager::update()
{
	//constructBuildings();

	//buildCombatUnits();

	buildWorkerUnits();
}

void BuildManager::buildWorkerUnits()
{
	// �ڿ��� 50�̻� ������ �ϲ� ������ �Ʒ��Ѵ�
	if (BWAPI::Broodwar->self()->minerals() >= 50) {
		buildWorkerUnit();
	}
}

void BuildManager::buildWorkerUnit()
{
	BWAPI::Unit producer = nullptr;

	BWAPI::UnitType targetUnitType;

	if (BWAPI::Broodwar->self()->getRace() == BWAPI::Races::Protoss) {
		targetUnitType = BWAPI::UnitTypes::Protoss_Probe;
	}
	else if (BWAPI::Broodwar->self()->getRace() == BWAPI::Races::Terran) {
		targetUnitType = BWAPI::UnitTypes::Terran_SCV;
	}
	else if (BWAPI::Broodwar->self()->getRace() == BWAPI::Races::Zerg) {
		targetUnitType = BWAPI::UnitTypes::Zerg_Drone;
	}
		
	// ResourceDepot �ǹ��� �ϲ� ������ ���� ������ �����̸� ������ ����Ѵ�
	for (auto & unit : BWAPI::Broodwar->self()->getUnits())
	{
		if (unit->getType().isResourceDepot() ){

			if (BWAPI::Broodwar->canMake(targetUnitType, unit) && unit->isTraining() == false && unit->isMorphing() == false) {

				producer = unit;

				if (BWAPI::Broodwar->self()->getRace() != BWAPI::Races::Zerg) {
					producer->train(targetUnitType);
				}
				else {
					producer->morph(targetUnitType);
				}
				break;
			}
		}
	}
}

void BuildManager::buildCombatUnits()
{
	BWAPI::UnitType targetUnitType;

	// �ڿ��� 100�̻� ������ ���� ���� ������ �Ʒ��Ѵ�
	if (BWAPI::Broodwar->self()->minerals() >= 100) {
		if (BWAPI::Broodwar->self()->getRace() == BWAPI::Races::Protoss) {
			targetUnitType = BWAPI::UnitTypes::Protoss_Zealot;
		}
		else if (BWAPI::Broodwar->self()->getRace() == BWAPI::Races::Terran) {
			targetUnitType = BWAPI::UnitTypes::Terran_Marine;
		}
		else if (BWAPI::Broodwar->self()->getRace() == BWAPI::Races::Zerg) {
			targetUnitType = BWAPI::UnitTypes::Zerg_Zergling;
		}

		trainUnit(targetUnitType);
	}
}

void BuildManager::trainUnit(BWAPI::UnitType targetUnitType)
{
	BWAPI::Unit producer = nullptr;
	BWAPI::UnitType producerUnitType = targetUnitType.whatBuilds().first;

	// targetUnitType�� ���� ������ ���°� �Ǹ� ������ ����Ѵ�
	for (auto & unit : BWAPI::Broodwar->self()->getUnits())
	{
		if (unit->getType() == producerUnitType) {
			if (BWAPI::Broodwar->canMake(targetUnitType, unit) && unit->isTraining() == false && unit->isMorphing() == false) {

				producer = unit;

				if (BWAPI::Broodwar->self()->getRace() != BWAPI::Races::Zerg) {
					producer->train(targetUnitType);
				}
				else {
					producer->morph(targetUnitType);
				}
				break;
			}
		}
	}
}


void BuildManager::constructBuildings()
{
	BWAPI::UnitType targetUnitType;

	// �ڿ��� 200�̻� ������ �������� ���� �ǹ��� �Ǽ� �Ѵ�
	if (BWAPI::Broodwar->self()->minerals() >= 200) {
		if (BWAPI::Broodwar->self()->getRace() == BWAPI::Races::Protoss) {
			targetUnitType = BWAPI::UnitTypes::Protoss_Gateway;
		}
		else if (BWAPI::Broodwar->self()->getRace() == BWAPI::Races::Terran) {
			targetUnitType = BWAPI::UnitTypes::Terran_Barracks;
		}
		else if (BWAPI::Broodwar->self()->getRace() == BWAPI::Races::Zerg) {
			targetUnitType = BWAPI::UnitTypes::Zerg_Spawning_Pool;
		}
		constructBuilding(targetUnitType);
	}

	// �ڿ��� 100�̻� �ְ�, ���ö��̰� ���������� SupplyProvider �� �ش��ϴ� ������ �����
	// ���ö��� ���ڴ� ��Ÿũ����Ʈ ���ӿ��� ǥ�õǴ� ������ 2��� ����ؾ��Ѵ�
	if (BWAPI::Broodwar->self()->minerals() >= 100
		&& BWAPI::Broodwar->self()->supplyUsed() + 6 > BWAPI::Broodwar->self()->supplyTotal()) {
		if (BWAPI::Broodwar->self()->getRace() == BWAPI::Races::Protoss) {
			targetUnitType = BWAPI::UnitTypes::Protoss_Pylon;
			constructBuilding(targetUnitType);
		}
		else if (BWAPI::Broodwar->self()->getRace() == BWAPI::Races::Terran) {
			targetUnitType = BWAPI::UnitTypes::Terran_Supply_Depot;
			constructBuilding(targetUnitType);
		}
		else if (BWAPI::Broodwar->self()->getRace() == BWAPI::Races::Zerg) {
			targetUnitType = BWAPI::UnitTypes::Zerg_Overlord;
			trainUnit(targetUnitType);
		}
	}
}

void BuildManager::constructBuilding(BWAPI::UnitType targetBuildingType)
{
	// �ϲ� �� �̳׶��� ����ϰ� ���� ���� �ϲ� �ϳ��� producer�� �����Ѵ�
	BWAPI::Unit producer = nullptr;
	BWAPI::UnitType producerUnitType = targetBuildingType.whatBuilds().first;

	for (auto & unit : BWAPI::Broodwar->self()->getUnits())
	{
		if (unit->getType() == producerUnitType) {
			if (BWAPI::Broodwar->canMake(targetBuildingType, unit)
				&& unit->isCompleted() 
				&& unit->isCarryingMinerals() == false
				&& unit->isConstructing() == false) {

				producer = unit;
				break;
			}
		}
	}

	if (producer == nullptr) {
		return;
	}

	// �ǹ��� �Ǽ��� ��ġ�� Start Location ��ó���� ã�´�
	// ó������ Start Location �ݰ� 4Ÿ�Ͽ� ���� ã�ƺ���, 
	// �������� Start Location �ݰ� 8Ÿ�Ͽ� ���� ã�ƺ��� ������ ������ ����������
	BWAPI::TilePosition seedPosition = InformationManager::Instance()._mainBaseLocations[InformationManager::Instance().selfPlayer]->getTilePosition();
	BWAPI::TilePosition desiredPosition = BWAPI::TilePositions::None;
	int maxRange = 32;
	bool constructionPlaceFound = false;

	for (int range = 4; range <= maxRange; range *= 2) {
		for (int i = seedPosition.x - range; i < seedPosition.x + range; i++) {
			for (int j = seedPosition.y - range; j < seedPosition.y + range; j++) {
				desiredPosition.x = i;
				desiredPosition.y = j;
				if (BWAPI::Broodwar->canBuildHere(desiredPosition, targetBuildingType, producer, true))	{
					constructionPlaceFound = true;
					break;
				}
			}
			if (constructionPlaceFound) break;
		}
		if (constructionPlaceFound) break;
	}

	if (constructionPlaceFound == true && desiredPosition != BWAPI::TilePositions::None) {
		producer->build(targetBuildingType, desiredPosition);
	}
}


