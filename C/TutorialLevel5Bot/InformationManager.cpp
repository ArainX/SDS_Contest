#include "InformationManager.h"

using namespace MyBot;

InformationManager::InformationManager()
{
	selfPlayer = BWAPI::Broodwar->self();
	enemyPlayer = BWAPI::Broodwar->enemy();

	selfRace = selfPlayer->getRace();
	enemyRace = enemyPlayer->getRace();

	_mainBaseLocations[selfPlayer] = BWTA::getStartLocation(BWAPI::Broodwar->self());
	_occupiedBaseLocations[selfPlayer] = std::list<BWTA::BaseLocation *>();
	_occupiedBaseLocations[selfPlayer].push_back(_mainBaseLocations[selfPlayer]);
	updateOccupiedRegions(BWTA::getRegion(_mainBaseLocations[selfPlayer]->getTilePosition()), BWAPI::Broodwar->self());

	_mainBaseLocations[enemyPlayer] = nullptr;
	_occupiedBaseLocations[enemyPlayer] = std::list<BWTA::BaseLocation *>();

	_firstChokePoint[selfPlayer] = nullptr;
	_firstChokePoint[enemyPlayer] = nullptr;
	_firstExpansionLocation[selfPlayer] = nullptr;
	_firstExpansionLocation[enemyPlayer] = nullptr;
	_secondChokePoint[selfPlayer] = nullptr;
	_secondChokePoint[enemyPlayer] = nullptr;

	updateChokePointAndExpansionLocation();
}

void InformationManager::update() 
{
	// ������ Eliminate �ǰų� Left ���� ��, enemy ���� �ڵ����� null �� ���� �����Ƿ�, null �� �������� �Ѵ�
	if (BWAPI::Broodwar->enemy() == nullptr) {
		enemyPlayer = nullptr;
	}
	else {
		if (BWAPI::Broodwar->enemy()->isDefeated() || BWAPI::Broodwar->enemy()->leftGame()) {
			enemyPlayer = nullptr;
		}
	}

	updateUnitsInfo();

	// occupiedBaseLocation �̳� occupiedRegion �� ���� �ȹٲ�Ƿ� ���� ���ص� �ȴ�
	if (BWAPI::Broodwar->getFrameCount() % 120 == 0) {
		updateBaseLocationInfo();
	}
}

void InformationManager::updateUnitsInfo() 
{
	// update units info
	for (auto & unit : BWAPI::Broodwar->enemy()->getUnits())
	{
		updateUnitInfo(unit);
	}

	for (auto & unit : BWAPI::Broodwar->self()->getUnits())
	{
		updateUnitInfo(unit);
	}

	// remove bad enemy units
	_unitData[enemyPlayer].removeBadUnits();
	_unitData[selfPlayer].removeBadUnits();
}

// �ش� unit �� ������ ������Ʈ �Ѵ� (UnitType, lastPosition, HitPoint ��)
void InformationManager::updateUnitInfo(BWAPI::Unit unit)
{
    if (unit->getPlayer() != selfPlayer && unit->getPlayer() != enemyPlayer) {
        return;
    }

	if (enemyRace == BWAPI::Races::Unknown && unit->getPlayer() == enemyPlayer) {
		enemyRace = unit->getType().getRace();
	}

    _unitData[unit->getPlayer()].updateUnitInfo(unit);
}

// ������ �ı�/����� ���, �ش� ���� ������ �����Ѵ�
void InformationManager::onUnitDestroy(BWAPI::Unit unit)
{ 
    if (unit->getType().isNeutral())
    {
        return;
    }

    _unitData[unit->getPlayer()].removeUnit(unit);
}

bool InformationManager::isCombatUnitType(BWAPI::UnitType type) const
{
	if (type == BWAPI::UnitTypes::Zerg_Lurker/* || type == BWAPI::UnitTypes::Protoss_Dark_Templar*/)
	{
		return false;
	}

	// check for various types of combat units
	if (type.canAttack() || 
		type == BWAPI::UnitTypes::Terran_Medic || 
		type == BWAPI::UnitTypes::Protoss_Observer ||
        type == BWAPI::UnitTypes::Terran_Bunker)
	{
		return true;
	}
		
	return false;
}

void InformationManager::getNearbyForce(std::vector<UnitInfo> & unitInfo, BWAPI::Position p, BWAPI::Player player, int radius) 
{
	// for each unit we know about for that player
	for (const auto & kv : getUnitData(player).getUnits())
	{
		const UnitInfo & ui(kv.second);

		// if it's a combat unit we care about
		// and it's finished! 
		if (isCombatUnitType(ui.type) && ui.completed)
		{
			// determine its attack range
			int range = 0;
			if (ui.type.groundWeapon() != BWAPI::WeaponTypes::None)
			{
				range = ui.type.groundWeapon().maxRange() + 40;
			}

			// if it can attack into the radius we care about
			if (ui.lastPosition.getDistance(p) <= (radius + range))
			{
				// add it to the vector
				unitInfo.push_back(ui);
			}
		}
		else if (ui.type.isDetector() && ui.lastPosition.getDistance(p) <= (radius + 250))
        {
			// add it to the vector
			unitInfo.push_back(ui);
        }
	}
}


int InformationManager::getNumUnits(BWAPI::UnitType t, BWAPI::Player player)
{
	return getUnitData(player).getNumUnits(t);
}


const UnitData & InformationManager::getUnitData(BWAPI::Player player) const
{
    return _unitData.find(player)->second;
}


InformationManager & InformationManager::Instance()
{
	static InformationManager instance;
	return instance;
}


void InformationManager::updateBaseLocationInfo()
{
	_occupiedRegions[selfPlayer].clear();
	_occupiedRegions[enemyPlayer].clear();
	_occupiedBaseLocations[selfPlayer].clear();
	_occupiedBaseLocations[enemyPlayer].clear();

	// enemy �� startLocation�� ���� �𸣴� ���
	if (_mainBaseLocations[enemyPlayer] == nullptr) {

		// how many start locations have we explored
		int exploredStartLocations = 0;
		bool enemyStartLocationFound = false;

		// an unexplored base location holder
		BWTA::BaseLocation * unexplored = nullptr;

		for (BWTA::BaseLocation * startLocation : BWTA::getStartLocations())
		{
			if (existsEnemyBuildingInRegion(BWTA::getRegion(startLocation->getTilePosition())))
			{
				if (enemyStartLocationFound == false) {
					enemyStartLocationFound = true;
					_mainBaseLocations[enemyPlayer] = startLocation;
				}
			}

			// if it's explored, increment
			if (BWAPI::Broodwar->isExplored(startLocation->getTilePosition()))
			{
				exploredStartLocations++;

			}
			// otherwise set it as unexplored base
			else
			{
				unexplored = startLocation;
			}
		}

		// if we've explored every start location except one, it's the enemy
		if (!enemyStartLocationFound && exploredStartLocations == ((int)BWTA::getStartLocations().size() - 1))
		{
			enemyStartLocationFound = true;
			_mainBaseLocations[enemyPlayer] = unexplored;
			_occupiedBaseLocations[enemyPlayer].push_back(unexplored);
		}
	}

	// update occupied base location
	// � Base Location ���� �Ʊ� �ǹ�, ���� �ǹ� ��� ȥ�����־ ���ÿ� ���� Player �� Occupy �ϰ� �ִ� ������ ������ �� �ִ�
	for (BWTA::BaseLocation * baseLocation : BWTA::getBaseLocations())
	{
		if (hasBuildingAroundBaseLocation(baseLocation, enemyPlayer))
		{
			_occupiedBaseLocations[enemyPlayer].push_back(baseLocation);
		}

		if (hasBuildingAroundBaseLocation(baseLocation, selfPlayer))
		{
			_occupiedBaseLocations[selfPlayer].push_back(baseLocation);
		}
	}

	// enemy�� mainBaseLocations�� �߰��� ��, �װ��� �ִ� �ǹ��� ��� �ı��� ��� _occupiedBaseLocations �߿��� _mainBaseLocations �� �����Ѵ�
	if (_mainBaseLocations[enemyPlayer] != nullptr) {
		if (existsEnemyBuildingInRegion(BWTA::getRegion(_mainBaseLocations[enemyPlayer]->getTilePosition())) == false)
		{
			for (std::list<BWTA::BaseLocation*>::const_iterator iterator = _occupiedBaseLocations[enemyPlayer].begin(), end = _occupiedBaseLocations[enemyPlayer].end(); iterator != end; ++iterator) {
				if (existsEnemyBuildingInRegion(BWTA::getRegion((*iterator)->getTilePosition())) == true) {
					_mainBaseLocations[enemyPlayer] = *iterator;
					break;
				}
			}
		}
	}

	// self�� mainBaseLocations�� ����, �װ��� �ִ� �ǹ��� ��� �ı��� ��� _occupiedBaseLocations �߿��� _mainBaseLocations �� �����Ѵ�
	if (_mainBaseLocations[selfPlayer] != nullptr) {
		if (existsMyBuildingInRegion(BWTA::getRegion(_mainBaseLocations[selfPlayer]->getTilePosition())) == false)
		{
			for (std::list<BWTA::BaseLocation*>::const_iterator iterator = _occupiedBaseLocations[selfPlayer].begin(), end = _occupiedBaseLocations[selfPlayer].end(); iterator != end; ++iterator) {
				if (existsMyBuildingInRegion(BWTA::getRegion((*iterator)->getTilePosition())) == true) {
					_mainBaseLocations[selfPlayer] = *iterator;
					break;
				}
			}
		}
	}

	// for each enemy building unit we know about
	for (const auto & kv : _unitData[enemyPlayer].getUnits())
	{
		const UnitInfo & ui(kv.second);
		if (ui.type.isBuilding())
		{
			updateOccupiedRegions(BWTA::getRegion(BWAPI::TilePosition(ui.lastPosition)), BWAPI::Broodwar->enemy());
		}
	}
	// for each of our building units
	for (const auto & kv : _unitData[selfPlayer].getUnits())
	{
		const UnitInfo & ui(kv.second);
		if (ui.type.isBuilding())
		{
			updateOccupiedRegions(BWTA::getRegion(BWAPI::TilePosition(ui.lastPosition)), BWAPI::Broodwar->self());
		}
	}

	updateChokePointAndExpansionLocation();
}

void InformationManager::updateChokePointAndExpansionLocation()
{
	if (_mainBaseLocations[selfPlayer]) {

		BWTA::BaseLocation* sourceBaseLocation = _mainBaseLocations[selfPlayer];

		_firstChokePoint[selfPlayer] = BWTA::getNearestChokepoint(sourceBaseLocation->getTilePosition());

		double tempDistance;
		double closestDistance = 1000000000;
		for (BWTA::BaseLocation * targetBaseLocation : BWTA::getBaseLocations())
		{
			if (targetBaseLocation == _mainBaseLocations[selfPlayer]) continue;

			tempDistance = BWTA::getGroundDistance(sourceBaseLocation->getTilePosition(), targetBaseLocation->getTilePosition());
			if (tempDistance < closestDistance && tempDistance > 0 ) {
				closestDistance = tempDistance;
				_firstExpansionLocation[selfPlayer] = targetBaseLocation;
			}
		}
		
		closestDistance = 1000000000;
		for (BWTA::Chokepoint * chokepoint : BWTA::getChokepoints())
		{
			if (chokepoint == _firstChokePoint[selfPlayer]) continue;

			tempDistance = BWTA::getGroundDistance(sourceBaseLocation->getTilePosition(), BWAPI::TilePosition(chokepoint->getCenter()));
			if (tempDistance < closestDistance && tempDistance > 0) {
				closestDistance = tempDistance;
				_secondChokePoint[selfPlayer] = chokepoint;
			}
		}
	}

	if (_mainBaseLocations[enemyPlayer]) {

		BWTA::BaseLocation* sourceBaseLocation = _mainBaseLocations[enemyPlayer];

		_firstChokePoint[enemyPlayer] = BWTA::getNearestChokepoint(sourceBaseLocation->getTilePosition());

		double tempDistance;
		double closestDistance = 1000000000;
		for (BWTA::BaseLocation * targetBaseLocation : BWTA::getBaseLocations())
		{
			if (targetBaseLocation == _mainBaseLocations[enemyPlayer]) continue;

			tempDistance = BWTA::getGroundDistance(sourceBaseLocation->getTilePosition(), targetBaseLocation->getTilePosition());
			if (tempDistance < closestDistance && tempDistance > 0) {
				closestDistance = tempDistance;
				_firstExpansionLocation[enemyPlayer] = targetBaseLocation;
			}
		}

		closestDistance = 1000000000;
		for (BWTA::Chokepoint * chokepoint : BWTA::getChokepoints())
		{
			if (chokepoint == _firstChokePoint[enemyPlayer]) continue;

			tempDistance = BWTA::getGroundDistance(sourceBaseLocation->getTilePosition(), BWAPI::TilePosition(chokepoint->getCenter()));
			if (tempDistance < closestDistance && tempDistance > 0) {
				closestDistance = tempDistance;
				_secondChokePoint[enemyPlayer] = chokepoint;
			}
		}
	}
}


void InformationManager::updateOccupiedRegions(BWTA::Region * region, BWAPI::Player player)
{
	// if the region is valid (flying buildings may be in nullptr regions)
	if (region)
	{
		// add it to the list of occupied regions
		_occupiedRegions[player].insert(region);
	}
}

// BaseLocation ���� �� �ȿ� player�� �ǹ��� ������ true �� ��ȯ�Ѵ�
bool InformationManager::hasBuildingAroundBaseLocation(BWTA::BaseLocation * baseLocation, BWAPI::Player player, int radius)
{
	// invalid regions aren't considered the same, but they will both be null
	if (!baseLocation)
	{
		return false;
	}

	for (const auto & kv : _unitData[player].getUnits())
	{
		const UnitInfo & ui(kv.second);
		if (ui.type.isBuilding())
		{
			BWAPI::TilePosition buildingPosition(ui.lastPosition);

			if (buildingPosition.x >= baseLocation->getTilePosition().x - radius && buildingPosition.x <= baseLocation->getTilePosition().x + radius
				&& buildingPosition.y >= baseLocation->getTilePosition().y - radius && buildingPosition.y <= baseLocation->getTilePosition().y + radius)
			{
				return true;
			}
		}
	}
	return false;
}


bool InformationManager::existsEnemyBuildingInRegion(BWTA::Region * region)
{
	// invalid regions aren't considered the same, but they will both be null
	if (!region)
	{
		return false;
	}

	for (const auto & kv : _unitData[enemyPlayer].getUnits())
	{
		const UnitInfo & ui(kv.second);
		if (ui.type.isBuilding())
		{
			if (BWTA::getRegion(BWAPI::TilePosition(ui.lastPosition)) == region)
			{
				return true;
			}
		}
	}

	return false;
}

bool InformationManager::existsMyBuildingInRegion(BWTA::Region * region)
{
	// invalid regions aren't considered the same, but they will both be null
	if (!region)
	{
		return false;
	}

	for (const auto & kv : _unitData[selfPlayer].getUnits())
	{
		const UnitInfo & ui(kv.second);
		if (ui.type.isBuilding() && ui.completed)
		{
			if (BWTA::getRegion(BWAPI::TilePosition(ui.lastPosition)) == region)
			{
				return true;
			}
		}
	}

	return false;
}

// �ش� Player �� UnitAndUnitInfoMap �� ����´�
const UnitAndUnitInfoMap & InformationManager::getUnitInfo(BWAPI::Player player) const
{
	return getUnitData(player).getUnits();
}

std::set<BWTA::Region *> & InformationManager::getOccupiedRegions(BWAPI::Player player)
{
	return _occupiedRegions[player];
}

std::list<BWTA::BaseLocation *> & InformationManager::getOccupiedBaseLocations(BWAPI::Player player)
{
	return _occupiedBaseLocations[player];
}

BWTA::BaseLocation * InformationManager::getMainBaseLocation(BWAPI::Player player)
{
	return _mainBaseLocations[player];
}

BWTA::Chokepoint * InformationManager::getFirstChokePoint(BWAPI::Player player)
{
	return _firstChokePoint[player];
}
BWTA::BaseLocation * InformationManager::getFirstExpansionLocation(BWAPI::Player player)
{
	return _firstExpansionLocation[player];
}

BWTA::Chokepoint * InformationManager::getSecondChokePoint(BWAPI::Player player)
{
	return _secondChokePoint[player];
}



BWAPI::UnitType InformationManager::getBasicCombatUnitType(BWAPI::Race race)
{
	if (race == BWAPI::Races::None) {
		race = selfRace;
	}

	if (race == BWAPI::Races::Protoss) {
		return BWAPI::UnitTypes::Protoss_Zealot;
	}
	else if (race == BWAPI::Races::Terran) {
		return BWAPI::UnitTypes::Terran_Marine;
	}
	else if (race == BWAPI::Races::Zerg) {
		return BWAPI::UnitTypes::Zerg_Zergling;
	}
	else {
		return BWAPI::UnitTypes::None;
	}
}

BWAPI::UnitType InformationManager::getAdvancedCombatUnitType(BWAPI::Race race)
{
	if (race == BWAPI::Races::None) {
		race = selfRace;
	}

	if (race == BWAPI::Races::Protoss) {
		return BWAPI::UnitTypes::Protoss_Dragoon;
	}
	else if (race == BWAPI::Races::Terran) {
		return BWAPI::UnitTypes::Terran_Medic;
	}
	else if (race == BWAPI::Races::Zerg) {
		return BWAPI::UnitTypes::Zerg_Hydralisk;
	}
	else {
		return BWAPI::UnitTypes::None;
	}
}

BWAPI::UnitType InformationManager::getBasicCombatBuildingType(BWAPI::Race race)
{
	if (race == BWAPI::Races::None) {
		race = selfRace;
	}

	if (race == BWAPI::Races::Protoss) {
		return BWAPI::UnitTypes::Protoss_Gateway;
	}
	else if (race == BWAPI::Races::Terran) {
		return BWAPI::UnitTypes::Terran_Barracks;
	}
	else if (race == BWAPI::Races::Zerg) {
		return BWAPI::UnitTypes::Zerg_Hatchery;
	}
	else {
		return BWAPI::UnitTypes::None;
	}
}

BWAPI::UnitType InformationManager::getObserverUnitType(BWAPI::Race race)
{
	if (race == BWAPI::Races::None) {
		race = selfRace;
	}

	if (race == BWAPI::Races::Protoss) {
		return BWAPI::UnitTypes::Protoss_Observer;
	}
	else if (race == BWAPI::Races::Terran) {
		return BWAPI::UnitTypes::Terran_Science_Vessel;
	}
	else if (race == BWAPI::Races::Zerg) {
		return BWAPI::UnitTypes::Zerg_Overlord;
	}
	else {
		return BWAPI::UnitTypes::None;
	}
}

BWAPI::UnitType	InformationManager::getBasicResourceDepotBuildingType(BWAPI::Race race)
{
	if (race == BWAPI::Races::None) {
		race = selfRace;
	}

	if (race == BWAPI::Races::Protoss) {
		return BWAPI::UnitTypes::Protoss_Nexus;
	}
	else if (race == BWAPI::Races::Terran) {
		return BWAPI::UnitTypes::Terran_Command_Center;
	}
	else if (race == BWAPI::Races::Zerg) {
		return BWAPI::UnitTypes::Zerg_Hatchery;
	}
	else {
		return BWAPI::UnitTypes::None;
	}
}
BWAPI::UnitType InformationManager::getRefineryBuildingType(BWAPI::Race race)
{
	if (race == BWAPI::Races::None) {
		race = selfRace;
	}

	if (race == BWAPI::Races::Protoss) {
		return BWAPI::UnitTypes::Protoss_Assimilator;
	}
	else if (race == BWAPI::Races::Terran) {
		return BWAPI::UnitTypes::Terran_Refinery;
	}
	else if (race == BWAPI::Races::Zerg) {
		return BWAPI::UnitTypes::Zerg_Extractor;
	}
	else {
		return BWAPI::UnitTypes::None;
	}

}

BWAPI::UnitType	InformationManager::getWorkerType(BWAPI::Race race)
{
	if (race == BWAPI::Races::None) {
		race = selfRace;
	}

	if (race == BWAPI::Races::Protoss) {
		return BWAPI::UnitTypes::Protoss_Probe;
	}
	else if (race == BWAPI::Races::Terran) {
		return BWAPI::UnitTypes::Terran_SCV;
	}
	else if (race == BWAPI::Races::Zerg) {
		return BWAPI::UnitTypes::Zerg_Drone;
	}
	else {
		return BWAPI::UnitTypes::None;
	}
}

BWAPI::UnitType InformationManager::getBasicSupplyProviderUnitType(BWAPI::Race race)
{
	if (race == BWAPI::Races::None) {
		race = selfRace;
	}

	if (race == BWAPI::Races::Protoss) {
		return BWAPI::UnitTypes::Protoss_Pylon;
	}
	else if (race == BWAPI::Races::Terran) {
		return BWAPI::UnitTypes::Terran_Supply_Depot;
	}
	else if (race == BWAPI::Races::Zerg) {
		return BWAPI::UnitTypes::Zerg_Overlord;
	}
	else {
		return BWAPI::UnitTypes::None;
	}
}

BWAPI::UnitType InformationManager::getBasicDefenseBuildingType(BWAPI::Race race)
{
	if (race == BWAPI::Races::None) {
		race = selfRace;
	}

	if (race == BWAPI::Races::Protoss) {
		return BWAPI::UnitTypes::Protoss_Pylon;
	}
	else if (race == BWAPI::Races::Terran) {
		return BWAPI::UnitTypes::Terran_Bunker;
	}
	else if (race == BWAPI::Races::Zerg) {
		return BWAPI::UnitTypes::Zerg_Creep_Colony;
	}
	else {
		return BWAPI::UnitTypes::None;
	}
}

BWAPI::UnitType InformationManager::getAdvancedDefenseBuildingType(BWAPI::Race race)
{
	if (race == BWAPI::Races::None) {
		race = selfRace;
	}

	if (race == BWAPI::Races::Protoss) {
		return BWAPI::UnitTypes::Protoss_Photon_Cannon;
	}
	else if (race == BWAPI::Races::Terran) {
		return BWAPI::UnitTypes::Terran_Missile_Turret;
	}
	else if (race == BWAPI::Races::Zerg) {
		return BWAPI::UnitTypes::Zerg_Sunken_Colony;
	}
	else {
		return BWAPI::UnitTypes::None;
	}
}

