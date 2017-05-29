#include "ConstructionPlaceFinder.h"

using namespace MyBot;

ConstructionPlaceFinder::ConstructionPlaceFinder()
{
	_reserveMap = std::vector< std::vector<bool> >(BWAPI::Broodwar->mapWidth(), std::vector<bool>(BWAPI::Broodwar->mapHeight(), false));

	_tilesToAvoid = std::set< BWAPI::TilePosition >();

	setTilesToAvoid();
}

ConstructionPlaceFinder & ConstructionPlaceFinder::Instance() 
{
    static ConstructionPlaceFinder instance;
    return instance;
}

BWAPI::TilePosition	ConstructionPlaceFinder::getBuildLocationWithSeedPositionAndStrategy(BWAPI::UnitType buildingType, BWAPI::TilePosition seedPosition, BuildOrderItem::SeedPositionStrategy seedPositionStrategy) const
{
	BWAPI::TilePosition desiredPosition = BWAPI::TilePositions::None;

	// seedPosition �� �Է��� ��� �� ��ó���� ã�´�
	// TODO ���� : �� ��ó���� ��ã���� ��� ���� �����غ� �����̴� 
	if (seedPosition != BWAPI::TilePositions::None  && seedPosition.isValid() )
	{
		//std::cout << "getBuildLocationNear " << seedPosition.x << ", " << seedPosition.y << std::endl;
		desiredPosition = getBuildLocationNear(buildingType, seedPosition);
	}
	// seedPosition �� �Է����� ���� ���
	else {

		BWTA::Chokepoint* tempChokePoint;
		BWTA::BaseLocation* tempBaseLocation;
		BWAPI::TilePosition tempTilePosition;
		BWTA::Region* tempBaseRegion;
		int vx, vy;
		double d, t;
		int bx, by;


		switch (seedPositionStrategy) {

		case BuildOrderItem::SeedPositionStrategy::MainBaseLocation:
			desiredPosition = getBuildLocationNear(buildingType, InformationManager::Instance().getMainBaseLocation(BWAPI::Broodwar->self())->getTilePosition());
			break;

		case BuildOrderItem::SeedPositionStrategy::MainBaseBackYard:
			tempBaseLocation = InformationManager::Instance().getMainBaseLocation(BWAPI::Broodwar->self());
			tempChokePoint = InformationManager::Instance().getFirstChokePoint(BWAPI::Broodwar->self());
			tempBaseRegion = BWTA::getRegion(tempBaseLocation->getPosition());

			//std::cout << "y";

			// (vx, vy) = BaseLocation �� ChokePoint �� ���� ���� = �Ÿ� d �� ���� t ����. ������ position
			// ��Ÿũ����Ʈ ��ǥ�� : ���������� ������ x �� ���� (��ī��Ʈ ��ǥ��� ����). �Ʒ��� ������ y�� ���� (y�ุ ��ī��Ʈ ��ǥ��� �ݴ�)
			// �ﰢ�Լ� ���� ��ī��Ʈ ��ǥ�迡�� ����ϹǷ�, vy�� ��ȣ �ݴ�� �ؼ� ���� t ���� ���� 

			// MainBaseLocation �� null �̰ų�, ChokePoint �� null �̸�, MainBaseLocation �������� ������ ���� �����Ѵ�
			if (tempBaseLocation == nullptr ) {
				//std::cout << "q";
				desiredPosition = getBuildLocationNear(buildingType, InformationManager::Instance().getMainBaseLocation(BWAPI::Broodwar->self())->getTilePosition());
				break;
			}
			else if (tempChokePoint == nullptr) {
				//std::cout << "r";
				desiredPosition = getBuildLocationNear(buildingType, InformationManager::Instance().getMainBaseLocation(BWAPI::Broodwar->self())->getTilePosition());
				break;
			}

			// BaseLocation ���� ChokePoint ���� ���͸� ���Ѵ�
			vx = tempChokePoint->getCenter().x - tempBaseLocation->getPosition().x;
			//std::cout << "vx : " << vx ;
			vy = (tempChokePoint->getCenter().y - tempBaseLocation->getPosition().y) * (-1);
			//std::cout << "vy : " << vy;
			d = std::sqrt(vx * vx + vy * vy) * 0.5; // BaseLocation �� ChokePoint �� �Ÿ����� ���� ª�� �Ÿ��� ����. BaseLocation�� �ִ� Region�� ��κ� ���簢�� �����̱� ����
			//std::cout << "d : " << d;
			t = std::atan2(vy, vx + 0.0001); // ���� ����
			//std::cout << "t : " << t;

			// cos(t+90), sin(t+180) �� �ﰢ�Լ� Trigonometric functions of allied angles �� �̿�. y�࿡ ���ؼ��� �ݴ��ȣ�� ����

			// BaseLocation ���� ChokePoint �ݴ��� ������ Back Yard : ��ī��Ʈ ��ǥ�迡�� (cos(t+180) = -cos(t), sin(t+180) = -sin(t))
			bx = tempBaseLocation->getTilePosition().x - (int)(d * std::cos(t) / TILE_SIZE);
			by = tempBaseLocation->getTilePosition().y + (int)(d * std::sin(t) / TILE_SIZE);
			//std::cout << "i";
			tempTilePosition = BWAPI::TilePosition(bx, by);
			// std::cout << "ConstructionPlaceFinder MainBaseBackYard tempTilePosition " << tempTilePosition.x << "," << tempTilePosition.y << std::endl;
			
			//std::cout << "k";
			// �ش� ������ ���� Region �� ���ϰ� Buildable �� Ÿ������ Ȯ��
			if (!tempTilePosition.isValid() || !BWAPI::Broodwar->isBuildable(tempTilePosition.x, tempTilePosition.y, false) || tempBaseRegion != BWTA::getRegion(BWAPI::Position(bx*TILE_SIZE, by*TILE_SIZE))) {
				//std::cout << "l";

				// BaseLocation ���� ChokePoint ���⿡ ���� ���������� 90�� ���� ������ Back Yard : ��ī��Ʈ ��ǥ�迡�� (cos(t-90) = sin(t),   sin(t-90) = - cos(t))
				bx = tempBaseLocation->getTilePosition().x + (int)(d * std::sin(t) / TILE_SIZE);
				by = tempBaseLocation->getTilePosition().y + (int)(d * std::cos(t) / TILE_SIZE);
				tempTilePosition = BWAPI::TilePosition(bx, by);
				// std::cout << "ConstructionPlaceFinder MainBaseBackYard tempTilePosition " << tempTilePosition.x << "," << tempTilePosition.y << std::endl;
				//std::cout << "m";

				if (!tempTilePosition.isValid() || !BWAPI::Broodwar->isBuildable(tempTilePosition.x, tempTilePosition.y, false)) {
					// BaseLocation ���� ChokePoint ���⿡ ���� �������� 90�� ���� ������ Back Yard : ��ī��Ʈ ��ǥ�迡�� (cos(t+90) = -sin(t),   sin(t+90) = cos(t))
					bx = tempBaseLocation->getTilePosition().x - (int)(d * std::sin(t) / TILE_SIZE);
					by = tempBaseLocation->getTilePosition().y - (int)(d * std::cos(t) / TILE_SIZE);
					tempTilePosition = BWAPI::TilePosition(bx, by);
					// std::cout << "ConstructionPlaceFinder MainBaseBackYard tempTilePosition " << tempTilePosition.x << "," << tempTilePosition.y << std::endl;

					if (!tempTilePosition.isValid() || !BWAPI::Broodwar->isBuildable(tempTilePosition.x, tempTilePosition.y, false) || tempBaseRegion != BWTA::getRegion(BWAPI::Position(bx*TILE_SIZE, by*TILE_SIZE))) {

						// BaseLocation ���� ChokePoint ���� ���� ������ Back Yard : ��ī��Ʈ ��ǥ�迡�� (cos(t),   sin(t))
						bx = tempBaseLocation->getTilePosition().x + (int)(d * std::cos(t) / TILE_SIZE);
						by = tempBaseLocation->getTilePosition().y - (int)(d * std::sin(t) / TILE_SIZE);
						tempTilePosition = BWAPI::TilePosition(bx, by);
						// std::cout << "ConstructionPlaceFinder MainBaseBackYard tempTilePosition " << tempTilePosition.x << "," << tempTilePosition.y << std::endl;
						//std::cout << "m";
					}

				}
			}
			//std::cout << "z";
			if (!tempTilePosition.isValid() || !BWAPI::Broodwar->isBuildable(tempTilePosition.x, tempTilePosition.y, false)) {
				desiredPosition = getBuildLocationNear(buildingType, tempBaseLocation->getTilePosition());
			}
			else {
				desiredPosition = getBuildLocationNear(buildingType, tempTilePosition);
			}
			//std::cout << "w";
			// std::cout << "ConstructionPlaceFinder MainBaseBackYard desiredPosition " << desiredPosition.x << "," << desiredPosition.y << std::endl;
			break;

		case BuildOrderItem::SeedPositionStrategy::FirstExpansionLocation:
			tempBaseLocation = InformationManager::Instance().getFirstExpansionLocation(BWAPI::Broodwar->self());
			if (tempBaseLocation) {
				desiredPosition = getBuildLocationNear(buildingType, tempBaseLocation->getTilePosition());
			}
			break;

		case BuildOrderItem::SeedPositionStrategy::FirstChokePoint:
			tempChokePoint = InformationManager::Instance().getFirstChokePoint(BWAPI::Broodwar->self());
			if (tempChokePoint) {
				desiredPosition = getBuildLocationNear(buildingType, BWAPI::TilePosition(tempChokePoint->getCenter()));
			}
			break;

		case BuildOrderItem::SeedPositionStrategy::SecondChokePoint:
			tempChokePoint = InformationManager::Instance().getSecondChokePoint(BWAPI::Broodwar->self());
			if (tempChokePoint) {
				desiredPosition = getBuildLocationNear(buildingType, BWAPI::TilePosition(tempChokePoint->getCenter()));
			}
			break;

		case BuildOrderItem::SeedPositionStrategy::SecondExpansionLocation:
			if (InformationManager::Instance().getOccupiedBaseLocations(BWAPI::Broodwar->self()).size() > 1) {
				for (BWTA::BaseLocation * baseLocation : InformationManager::Instance().getOccupiedBaseLocations(BWAPI::Broodwar->self())) {

					if (baseLocation != InformationManager::Instance().getMainBaseLocation(BWAPI::Broodwar->self())
						&& baseLocation != InformationManager::Instance().getFirstExpansionLocation(BWAPI::Broodwar->self()))
					{
						tempTilePosition = baseLocation->getTilePosition();
					}
				}
				if (tempTilePosition != BWAPI::TilePositions::None) {
					desiredPosition = tempTilePosition;
				}
			}
			break;

		}
	}

	return desiredPosition;
}

BWAPI::TilePosition	ConstructionPlaceFinder::getBuildLocationNear(BWAPI::UnitType buildingType, BWAPI::TilePosition desiredPosition) const
{
	if (buildingType.isRefinery())
	{
		//std::cout << "getRefineryPositionNear "<< std::endl;

		return getRefineryPositionNear(desiredPosition);
	}

	if (BWAPI::Broodwar->self()->getRace() == BWAPI::Races::Protoss) {
		// special easy case of having no pylons
		if (buildingType.requiresPsi() && BWAPI::Broodwar->self()->completedUnitCount(BWAPI::UnitTypes::Protoss_Pylon) == 0)
		{
			return BWAPI::TilePositions::None;
		}
	}

	if (desiredPosition == BWAPI::TilePositions::None || desiredPosition == BWAPI::TilePositions::Unknown || desiredPosition == BWAPI::TilePositions::Invalid || desiredPosition.isValid() == false)
	{
		desiredPosition = InformationManager::Instance().getMainBaseLocation(BWAPI::Broodwar->self())->getTilePosition();
	}

	BWAPI::TilePosition testPosition = BWAPI::TilePositions::None;

	// TODO ���� : �Ǽ� ��ġ Ž�� ����� ConstructionPlaceSearchMethod::SpiralMethod �� �ϴµ�, �� ���� ����� �����غ� �����̴�
	size_t constructionPlaceSearchMethod = ConstructionPlaceSearchMethod::SpiralMethod;
	
	// �Ϲ����� �ǹ��� ���ؼ��� �ǹ� ũ�⺸�� Config::Macro::BuildingSpacing ĭ ��ŭ �����¿�� �� �а� ���������� �ξ �� �ڸ��� �˻��Ѵ�
	int buildingGapSpace = Config::Macro::BuildingSpacing;

	// ResourceDepot (Nexus, Command Center, Hatchery),
	// Protoss_Pylon, Terran_Supply_Depot, 
	// Protoss_Photon_Cannon, Terran_Bunker, Terran_Missile_Turret, Zerg_Creep_Colony �� �ٸ� �ǹ� �ٷ� ���� �ٿ� ���� ��찡 �����Ƿ� 
	// buildingGapSpace�� �ٸ� Config ������ �����ϵ��� �Ѵ�
	if (buildingType.isResourceDepot()) {
		buildingGapSpace = Config::Macro::BuildingResourceDepotSpacing;		
	}
	else if (buildingType == BWAPI::UnitTypes::Protoss_Pylon) {
		int numPylons = BWAPI::Broodwar->self()->completedUnitCount(BWAPI::UnitTypes::Protoss_Pylon);
		
		// Protoss_Pylon �� Ư�� ���� 2�� �Ǽ��Ҷ��� Config::Macro::BuildingPylonEarlyStageSpacing ������ �����Ѵ�
		if (numPylons < 3) {
			buildingGapSpace = Config::Macro::BuildingPylonEarlyStageSpacing;
		}
		else {
			buildingGapSpace = Config::Macro::BuildingPylonSpacing;
		}
	}
	else if (buildingType == BWAPI::UnitTypes::Terran_Supply_Depot) {
		buildingGapSpace = Config::Macro::BuildingSupplyDepotSpacing;
	}
	else if (buildingType == BWAPI::UnitTypes::Protoss_Photon_Cannon || buildingType == BWAPI::UnitTypes::Terran_Bunker 
		|| buildingType == BWAPI::UnitTypes::Terran_Missile_Turret || buildingType == BWAPI::UnitTypes::Zerg_Creep_Colony) {
		buildingGapSpace = Config::Macro::BuildingDefenseTowerSpacing;
	}

	while (buildingGapSpace >= 0) {

		testPosition = getBuildLocationNear(buildingType, desiredPosition, buildingGapSpace, constructionPlaceSearchMethod);

		// std::cout << "ConstructionPlaceFinder testPosition " << testPosition.x << "," << testPosition.y << std::endl;

		if (testPosition != BWAPI::TilePositions::None && testPosition != BWAPI::TilePositions::Invalid)
			return testPosition;
				
		// ã�� �� ���ٸ�, buildingGapSpace ���� �ٿ��� �ٽ� Ž���Ѵ�
		// buildingGapSpace ���� 1�̸� ���������� ���������� ��찡 ����  �����ϵ��� �Ѵ� 
		// 4 -> 3 -> 2 -> 0 -> Ž�� ����
		//      3 -> 2 -> 0 -> Ž�� ���� 
		//           1 -> 0 -> Ž�� ����
		if (buildingGapSpace > 2) {
			buildingGapSpace -= 1;
		}
		else if (buildingGapSpace == 2){
			buildingGapSpace = 0;
		}
		else if (buildingGapSpace == 1){
			buildingGapSpace = 0;
		}
		else {
			break;
		}
	}

	return BWAPI::TilePositions::None;
}

BWAPI::TilePosition	ConstructionPlaceFinder::getBuildLocationNear(BWAPI::UnitType buildingType, BWAPI::TilePosition desiredPosition, int buildingGapSpace, size_t constructionPlaceSearchMethod) const
{
	// std::cout << std::endl << "getBuildLocationNear " << buildingType.getName().c_str() << " " << desiredPosition.x << "," << desiredPosition.y 
	//	<< " gap " << buildingGapSpace << " method " << constructionPlaceSearchMethod << std::endl;

	//returns a valid build location near the desired tile position (x,y).
	BWAPI::TilePosition resultPosition = BWAPI::TilePositions::None;
	BWAPI::TilePosition tempPosition;
	ConstructionTask b(buildingType, desiredPosition);

	// maxRange �� �������� �ʰų�, maxRange �� 128���� �����ϸ� ���� ��ü�� �� Ž���ϴµ�, �ſ� �������Ӹ� �ƴ϶�, ��κ��� ��� ���ʿ��� Ž���� �ȴ�
	// maxRange �� 16 ~ 64�� �����ϴ�
	int maxRange = 32; // maxRange = BWAPI::Broodwar->mapWidth()/4;
	bool isPossiblePlace = false;
		
	if (constructionPlaceSearchMethod == ConstructionPlaceSearchMethod::SpiralMethod)
	{
		// desiredPosition ���κ��� �����ؼ� spiral �ϰ� Ž���ϴ� ���
		// ó������ �Ʒ� ���� (0,1) -> ����������(1,0) -> ����(0,-1) -> ��������(-1,0) -> �Ʒ���(0,1) -> ..
		int currentX = desiredPosition.x;
		int currentY = desiredPosition.y;
		int spiralMaxLength = 1;
		int numSteps = 0;
		boolean isFirstStep = true;

		int spiralDirectionX = 0;
		int spiralDirectionY = 1;
		while (spiralMaxLength < maxRange)
		{
			if (currentX >= 0 && currentX < BWAPI::Broodwar->mapWidth() && currentY >= 0 && currentY <BWAPI::Broodwar->mapHeight()) {

				isPossiblePlace = canBuildHereWithSpace(BWAPI::TilePosition(currentX, currentY), b, buildingGapSpace);

				if (isPossiblePlace) {
					resultPosition = BWAPI::TilePosition(currentX, currentY);
					break;
				}
			}

			currentX = currentX + spiralDirectionX;
			currentY = currentY + spiralDirectionY;
			numSteps++;

			// �ٸ� �������� ��ȯ�Ѵ�
			if (numSteps == spiralMaxLength)
			{
				numSteps = 0;

				if (!isFirstStep)
					spiralMaxLength++;

				isFirstStep = !isFirstStep;

				if (spiralDirectionX == 0)
				{
					spiralDirectionX = spiralDirectionY;
					spiralDirectionY = 0;
				}
				else
				{
					spiralDirectionY = -spiralDirectionX;
					spiralDirectionX = 0;
				}
			}
		}
	}
	else if (constructionPlaceSearchMethod == ConstructionPlaceSearchMethod::NewMethod) {
	}

	return resultPosition;
}


bool ConstructionPlaceFinder::canBuildHereWithSpace(BWAPI::TilePosition position, const ConstructionTask & b, int buildingGapSpace) const
{
	//if we can't build here, we of course can't build here with space
	if (!canBuildHere(position, b))
	{
		return false;
	}

	// height and width of the building
	int width(b.type.tileWidth());
	int height(b.type.tileHeight());

	// define the rectangle of the building spot
	// �ǹ� ũ�⺸�� �����¿�� �� ū �簢��
	int startx;
	int starty;
	int endx;
	int endy;

	bool horizontalOnly = false;

	// Refinery �� ��� GapSpace�� üũ�� �ʿ� ����
	if (b.type.isRefinery())
	{
	}
	// Addon Ÿ���� �ǹ��� ��쿡��, �� Addon �ǹ� ���ʿ� whatBuilds �ǹ��� �ִ����� üũ�Ѵ�
	if (b.type.isAddon())
	{
		const BWAPI::UnitType builderType = b.type.whatBuilds().first;

		BWAPI::TilePosition builderTile(position.x - builderType.tileWidth(), position.y + 2 - builderType.tileHeight());

		startx = builderTile.x - buildingGapSpace;
		starty = builderTile.y - buildingGapSpace;
		endx = position.x + width + buildingGapSpace;
		endy = position.y + height + buildingGapSpace;

		// builderTile�� Lifted �ǹ��� �ƴϰ� whatBuilds �ǹ��� �ƴ� �ǹ��� �ִ��� üũ
		for (int i = 0; i <= builderType.tileWidth(); ++i)
		{
			for (int j = 0; j <= builderType.tileHeight(); ++j)
			{
				for (auto & unit : BWAPI::Broodwar->getUnitsOnTile(builderTile.x + i, builderTile.y + j))
				{
					if ((unit->getType() != builderType) && (!unit->isLifted()))
					{
						return false;
					}
				}
			}
		}
	}
	else 
	{
		//make sure we leave space for add-ons. These types of units can have addon:
		if (b.type == BWAPI::UnitTypes::Terran_Command_Center ||
			b.type == BWAPI::UnitTypes::Terran_Factory ||
			b.type == BWAPI::UnitTypes::Terran_Starport ||
			b.type == BWAPI::UnitTypes::Terran_Science_Facility)
		{
			width += 2;
		}

		// �����¿쿡 buildingGapSpace ��ŭ ������ ����
		if (horizontalOnly == false)
		{
			startx = position.x - buildingGapSpace;
			starty = position.y - buildingGapSpace;
			endx = position.x + width + buildingGapSpace;
			endy = position.y + height + buildingGapSpace;
		}
		// �¿�θ� buildingGapSpace ��ŭ ������ ����
		else {
			startx = position.x - buildingGapSpace;
			starty = position.y;
			endx = position.x + width + buildingGapSpace;
			endy = position.y + height;
		}

		// �׶����� �ǹ��� ��� �ٸ� �ǹ��� Addon ������ Ȯ�����ֱ� ����, ���� 2ĭ�� �ݵ�� GapSpace�� �ǵ��� �Ѵ�
		if (b.type.getRace() == BWAPI::Races::Terran) {
			if (buildingGapSpace < 2) {
				startx = position.x - 2;
				endx = position.x + width + buildingGapSpace;
			}
		}

		// �ǹ��� ������ ���� �� �ƴ϶� ������ buildingGapSpace �������� �� ����ִ���, �Ǽ������� Ÿ������, ����Ǿ��ִ°��� �ƴ���, TilesToAvoid �� �ش����� �ʴ��� üũ
		for (int x = startx; x < endx; x++)
		{
			for (int y = starty; y < endy; y++)
			{
				// if we can't build here, or space is reserved, we can't build here
				if (isBuildableTile(b, x, y) == false)
				{
					return false;
				}

				if (isReservedTile(x, y)) {
					return false;
				}

				// ResourceDepot / Addon �ǹ��� �ƴ� �Ϲ� �ǹ��� ���, BaseLocation �� Geyser ���� Ÿ�� (TilesToAvoid) ���� �ǹ��� ���� �ʴ´�
				if (b.type.isResourceDepot() == false && b.type.isAddon() == false) {
					if (isTilesToAvoid(x, y)) {
						return false;
					}
				}
			}
		}
	}

	// if this rectangle doesn't fit on the map we can't build here
	if (startx < 0 || starty < 0 || endx > BWAPI::Broodwar->mapWidth() || endx < position.x + width || endy > BWAPI::Broodwar->mapHeight())
	{
		return false;
	}

	return true;
}

bool ConstructionPlaceFinder::canBuildHere(BWAPI::TilePosition position, const ConstructionTask & b) const
{
	/*if (!b.type.isRefinery() && !InformationManager::Instance().tileContainsUnit(position))
	{
	return false;
	}*/

	// This function checks for creep, power, and resource distance requirements in addition to the tiles' buildability and possible units obstructing the build location.
	if (!BWAPI::Broodwar->canBuildHere(position, b.type, b.constructionWorker))
	{
		return false;
	}
	
	// check the reserve map
	for (int x = position.x; x < position.x + b.type.tileWidth(); x++)
	{
		for (int y = position.y; y < position.y + b.type.tileHeight(); y++)
		{
			if (_reserveMap[x][y])
			{
				return false;
			}
		}
	}

	// if it overlaps a base location return false
	// ResourceDepot �ǹ��� �ƴ� �ٸ� �ǹ��� BaseLocation ��ġ�� ���� ���ϵ��� �Ѵ�
	if (isOverlapsWithBaseLocation(position, b.type))
	{
		return false;
	}

	return true;
}

BWAPI::TilePosition ConstructionPlaceFinder::getRefineryPositionNear(BWAPI::TilePosition seedPosition) const
{
	if (seedPosition == BWAPI::TilePositions::None || seedPosition == BWAPI::TilePositions::Unknown || seedPosition == BWAPI::TilePositions::Invalid || seedPosition.isValid() == false)
	{
		seedPosition = InformationManager::Instance().getMainBaseLocation(BWAPI::Broodwar->self())->getTilePosition();
	}
	
	BWAPI::TilePosition closestGeyser = BWAPI::TilePositions::None;
	double minGeyserDistanceFromSeedPosition = 100000000;

	// for each geyser
	for (auto & geyser : BWAPI::Broodwar->getStaticGeysers())
	{
		// geyser->getType() �� �ϸ�, Unknown �̰ų�, Resource_Vespene_Geyser �̰ų�, Terran_Refinery �� ���� �ǹ����� ������, 
		// �ǹ��� �ı��Ǿ �ڵ����� Resource_Vespene_Geyser �� ���ư��� �ʴ´�

		BWAPI::Position geyserPos = geyser->getInitialPosition();
		BWAPI::TilePosition geyserTilePos = geyser->getInitialTilePosition();

		//std::cout << " geyserTilePos " << geyserTilePos.x << "," << geyserTilePos.y << std::endl;

		// �̹� ����Ǿ��ִ°�
		if (isReservedTile(geyserTilePos.x, geyserTilePos.y)) {
			continue;
		}

		// if it is not connected fron seedPosition, it is located in another island
		if (!BWTA::isConnected(seedPosition, geyserTilePos))
		{
			continue;
		}

		// �̹� ������ �ִ°�
		bool refineryAlreadyBuilt = false;
		BWAPI::Unitset alreadyBuiltUnits = BWAPI::Broodwar->getUnitsInRadius(geyserPos, 4 * TILE_SIZE);
		for (auto & u : alreadyBuiltUnits) {
			if (u->getType().isRefinery() && u->exists()) {
				refineryAlreadyBuilt = true;
			}
		}

		//std::cout << " geyser TilePos is not reserved, is connected, is not refineryAlreadyBuilt" << std::endl;

		if (refineryAlreadyBuilt == false)
		{
			double thisDistance = BWTA::getGroundDistance(BWAPI::TilePosition(geyserPos.x / TILE_SIZE, geyserPos.y / TILE_SIZE), seedPosition);

			if (thisDistance < minGeyserDistanceFromSeedPosition)
			{
				minGeyserDistanceFromSeedPosition = thisDistance;
				closestGeyser = geyser->getInitialTilePosition();
			}
		}

	}

	return closestGeyser;
}

// �ش� tile �� BaseLocation (ResourceDepot �ǹ��� ���� ���) �� ��ġ���� üũ�Ѵ�
bool ConstructionPlaceFinder::isOverlapsWithBaseLocation(BWAPI::TilePosition tile, BWAPI::UnitType type) const
{
	// if it's a resource depot we don't care if it overlaps
	if (type.isResourceDepot())
	{
		return false;
	}

	// dimensions of the proposed location
	int tx1 = tile.x;
	int ty1 = tile.y;
	int tx2 = tx1 + type.tileWidth();
	int ty2 = ty1 + type.tileHeight();

	// for each base location
	for (BWTA::BaseLocation * base : BWTA::getBaseLocations())
	{
		// dimensions of the base location
		int bx1 = base->getTilePosition().x;
		int by1 = base->getTilePosition().y;
		int bx2 = bx1 + InformationManager::Instance().getBasicResourceDepotBuildingType().tileWidth();
		int by2 = by1 + InformationManager::Instance().getBasicResourceDepotBuildingType().tileHeight();

		// conditions for non-overlap are easy
		bool noOverlap = (tx2 < bx1) || (tx1 > bx2) || (ty2 < by1) || (ty1 > by2);

		// if the reverse is true, return true
		if (!noOverlap)
		{
			return true;
		}
	}

	// otherwise there is no overlap
	return false;
}

//returns true if this tile is currently isBuildableTile, takes into account units on tile
// Broodwar �� isBuildable, ���� ������, �ٸ� ���ֵ��� üũ
bool ConstructionPlaceFinder::isBuildableTile(const ConstructionTask & b, int x, int y) const
{
	BWAPI::TilePosition tp(x, y);
	if (!tp.isValid())
	{
		return false;
	}

	// �� ������ �Ӹ� �ƴ϶� ���� �����͸� ��� ����ؼ� isBuildable üũ
	//if (BWAPI::Broodwar->isBuildable(x, y) == false)
	if (BWAPI::Broodwar->isBuildable(x, y, true) == false)
	{
		return false;
	}

	// constructionWorker �̿��� �ٸ� ������ ������ false�� �����Ѵ�
	for (auto & unit : BWAPI::Broodwar->getUnitsOnTile(x, y))
	{
		if ((b.constructionWorker != nullptr) && (unit != b.constructionWorker))
		{
			return false;
		}
	}

	return true;
}

void ConstructionPlaceFinder::reserveTiles(BWAPI::TilePosition position, int width, int height)
{
	int rwidth = _reserveMap.size();
	int rheight = _reserveMap[0].size();
	for (int x = position.x; x < position.x + width && x < rwidth; x++)
	{
		for (int y = position.y; y < position.y + height && y < rheight; y++)
		{
			_reserveMap[x][y] = true;
		}
	}
}

void ConstructionPlaceFinder::freeTiles(BWAPI::TilePosition position, int width, int height)
{
	int rwidth = _reserveMap.size();
	int rheight = _reserveMap[0].size();

	for (int x = position.x; x < position.x + width && x < rwidth; x++)
	{
		for (int y = position.y; y < position.y + height && y < rheight; y++)
		{
			_reserveMap[x][y] = false;
		}
	}
}

// �ǹ� �Ǽ� ����Ǿ��ִ� Ÿ������ üũ
bool ConstructionPlaceFinder::isReservedTile(int x, int y) const
{
	int rwidth = _reserveMap.size();
	int rheight = _reserveMap[0].size();
	if (x < 0 || y < 0 || x >= rwidth || y >= rheight)
	{
		return false;
	}

	return _reserveMap[x][y];
}


std::vector< std::vector<bool> > & ConstructionPlaceFinder::getReserveMap() {
	return _reserveMap;
}

bool ConstructionPlaceFinder::isTilesToAvoid(int x, int y) const
{
	for (auto & t : _tilesToAvoid) {
		if (t.x == x && t.y == y) {
			return true;
		}
	}

	return false;
}


void ConstructionPlaceFinder::setTilesToAvoid()
{
	// ResourceDepot �ǹ��� width = 4 Ÿ��, height = 3 Ÿ��
	// Geyser ��            width = 4 Ÿ��, height = 2 Ÿ��
	// Mineral ��           width = 2 Ÿ��, height = 1 Ÿ��

	for (BWTA::BaseLocation * base : BWTA::getBaseLocations())
	{
		// Island �� ��� �ǹ� ���� ������ ���������� ���� ������ �ǹ� ������ ������ ���� �ʴ´�
		if (base->isIsland()) continue;
		if (BWTA::isConnected(base->getTilePosition(), InformationManager::Instance().getMainBaseLocation(BWAPI::Broodwar->self())->getTilePosition()) == false) continue;

		// dimensions of the base location
		int bx0 = base->getTilePosition().x;
		int by0 = base->getTilePosition().y;
		int bx4 = base->getTilePosition().x + 4;
		int by3 = base->getTilePosition().y + 3;

		// BaseLocation �� Geyser ������ Ÿ���� BWTA::getShortestPath �� ����ؼ� ���� �� _tilesToAvoid �� �߰�
		for (auto & geyser : base->getGeysers())
		{
			BWAPI::TilePosition closeGeyserPosition = geyser->getInitialTilePosition();

			// dimensions of the closest geyser
			int gx0 = closeGeyserPosition.x;
			int gy0 = closeGeyserPosition.y;
			int gx4 = closeGeyserPosition.x + 4;
			int gy2 = closeGeyserPosition.y + 2;

			for (int i = bx0; i < bx4; i++) {
				for (int j = by0; j < by3; j++) {
					for (int k = gx0; k < gx4; k++) {
						for (int l = gy0; l < gy2; l++) {
							std::vector<BWAPI::TilePosition> tileList = BWTA::getShortestPath(BWAPI::TilePosition(i, j), BWAPI::TilePosition(k, l));
							for (auto & t : tileList) {
								_tilesToAvoid.insert(t);
							}
						}
					}
				}
			}

			/*
			// Geyser �� Base Location �� ������⿡ �ִ°��� ���� �ּ����� Ÿ�ϸ� �Ǵ��ؼ� tilesToAvoid �� �߰��ϴ� ����� �ִ�
			//
			//    11�ù���   12�ù���  1�ù���
			//
			//     9�ù���             3�ù���
			//
			//     7�ù���    6�ù���  5�ù���
			int whichPosition = 0;

			// dimensions of the tilesToAvoid
			int vx0 = 0;
			int vx1 = 0;
			int vy0 = 0;
			int vy1 = 0;

			// 11�� ����
			if (gx0 < bx0 && gy0 < by0) {
				vx0 = gx0 + 1; // Geyser �� �߾�
				vy0 = gy0;     // Geyser �� ���
				vx1 = bx0 + 3; // ResourceDepot �� �߾�
				vy1 = by0;     // ResourceDepot�� ���
			}
			// 9�� ����
			else if (gx0 < bx0 && gy0 <= by3) {
				vx0 = gx4; // Geyser �� �����ʳ�
				vy0 = gy0; // Geyser �� ���
				vx1 = bx0; // ResourceDepot �� ���ʳ�
				vy1 = gy2; // Geyser �� �ϴ� 
			}
			// 7�� ����
			else if (gx0 < bx0 && gy2 > by3) {
				vx0 = gx0 + 1; // Geyser �� ��� �߾�
				vy0 = by3;     // ResourceDepot �� �ϴ�
				vx1 = bx0 + 3; // ResourceDepot �� �ϴ� �߾�
				vy1 = gy0;     // Geyser �� ���
			}
			// 6�� ����
			else if (gx0 < bx4 && gy0 > by3) {
				vx0 = bx0 + 1; // ResourceDepot �� �ϴ� �߾�
				vy0 = by3;     // ResourceDepot �� �ϴ� 
				vx1 = gx0 + 3; // Geyser �� ��� �߾�
				vy1 = gy0;     // Geyser �� ���
			}
			// 12�� ����
			else if (gx0 < bx4 && gy0 < by0) {
				vx0 = gx0;     // Geyser �� �ϴ� ���ʳ�
				vy0 = gy2; 
				vx1 = gx0 + 3; // Geyser �� �߾�
				vy1 = by0;     // ResourceDepot �� ���
			}
			// 1�� ����
			else if (gx0 > bx0 && gy0 < by0) {
				vx0 = bx0 + 2; // ResourceDepot �� ��� �߾�
				vy0 = gy0 + 1; // Geyser �� �ϴ�
				vx1 = gx0 + 2; // Geyser �� �߾�
				vy1 = by0 + 1; // ResourceDepot �� ���
			}
			// 5�� ����
			else if (gx0 > bx0 && gy0 >= by3) {
				vx0 = bx0 + 2; // ResourceDepot �� �ϴ� �߾�
				vy0 = by0 + 2; // ResourceDepot �� �ϴ�
				vx1 = gx0 + 2; // Geyser �� �߾�
				vy1 = gy0 + 1; // Geyser �� �ϴ�
			}
			// 3�� ����
			else if (gx0 > bx0 && gy0 >= by0) {
				vx0 = bx4; // ResourceDepot �� �����ʳ�
				vy0 = gy0; // Geyser �� ���
				vx1 = gx0; // Geyser �� ���� ��
				vy1 = gy2; // Geyser �� �ϴ�
			}

			for (int i = vx0; i < vx1; i++) {
				for (int j = vy0; j < vy1; j++) {
					_tilesToAvoid.insert(BWAPI::TilePosition(i, j));
				}
			}
			*/

		}

		// BaseLocation �� Mineral ������ Ÿ���� BWTA::getShortestPath �� ����ؼ� ���� �� _tilesToAvoid �� �߰�
		for (auto & mineral : base->getMinerals())
		{
			BWAPI::TilePosition closeMineralPosition = mineral->getInitialTilePosition();

			// dimensions of the closest mineral
			int mx0 = closeMineralPosition.x;
			int my0 = closeMineralPosition.y;
			int mx2 = mx0 + 2;
			int my1 = my0 + 1;

			for (int i = bx0; i < bx4; i++) {
				for (int j = by0; j < by3; j++) {
					for (int k = mx0; k < mx2; k++) {
						std::vector<BWAPI::TilePosition> tileList = BWTA::getShortestPath(BWAPI::TilePosition(i, j), BWAPI::TilePosition(k, my0));
						for (auto & t : tileList) {
							_tilesToAvoid.insert(t);
						}
					}
				}
			}
		}
	}
}

std::set< BWAPI::TilePosition > & ConstructionPlaceFinder::getTilesToAvoid() {
	return _tilesToAvoid;
}
