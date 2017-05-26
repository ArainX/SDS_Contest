import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import bwapi.Position;
import bwapi.Race;
import bwapi.TilePosition;
import bwapi.Unit;
import bwapi.UnitType;
import bwta.BWTA;
import bwta.BaseLocation;
import bwta.Chokepoint;
import bwta.Region;

/// �Ǽ���ġ Ž���� ���� class
public class ConstructionPlaceFinder {

	/// �Ǽ���ġ Ž�� ���
	public enum ConstructionPlaceSearchMethod { 
		SpiralMethod,	///< ���������� �� �а� Ȯ���ϸ� Ž�� 
		NewMethod 		///< ����
	};
	
	/// �ǹ� �Ǽ� ���� Ÿ���� �����س��� ���� 2���� �迭
	/// TilePosition �����̱� ������ ���� 128*128 ����� �ȴ�
	/// �����, �ǹ��� �̹� ������ Ÿ���� �������� �ʴ´�
	private boolean[][] reserveMap = new boolean[128][128];
	
	/// BaseLocation �� Mineral / Geyser ������ Ÿ�ϵ��� ��� �ڷᱸ��. ���⿡�� Addon �̿ܿ��� �ǹ��� ���� �ʵ��� �մϴ�	
	private Set<TilePosition> tilesToAvoid = new HashSet<TilePosition>();
	
	private static ConstructionPlaceFinder instance = new ConstructionPlaceFinder();
	
	private static boolean isInitialized = false;
	
	/// static singleton ��ü�� �����մϴ�
	public static ConstructionPlaceFinder Instance() {
		if (isInitialized == false) {
			instance.setTilesToAvoid();
			isInitialized = true;
		}
		return instance;
	}

	/// seedPosition �� seedPositionStrategy �Ķ���͸� Ȱ���ؼ� �ǹ� �Ǽ� ���� ��ġ�� Ž���ؼ� �����մϴ�
	/// seedPosition �������� ������ ���� �����ϰų�, seedPositionStrategy �� ���� ���� �м���� �ش� ���� �������� ������ ���� �����մϴ�
	/// seedPosition, seedPositionStrategy �� �Է����� ������, MainBaseLocation �������� ������ ���� �����մϴ�
	public final TilePosition getBuildLocationWithSeedPositionAndStrategy(UnitType buildingType, TilePosition seedPosition, BuildOrderItem.SeedPositionStrategy seedPositionStrategy)
	{
		TilePosition desiredPosition = TilePosition.None;

		// seedPosition �� �Է��� ��� �� ��ó���� ã�´�
		// TODO ���� : �� ��ó���� ��ã���� ��� ���� �����غ� �����̴� 
		if (seedPosition != TilePosition.None  && seedPosition.isValid() )
		{
			//std::cout << "getBuildLocationNear " << seedPosition.x << ", " << seedPosition.y << std::endl;
			desiredPosition = getBuildLocationNear(buildingType, seedPosition);
		}
		// seedPosition �� �Է����� ���� ���
		else {
			Chokepoint tempChokePoint;
			BaseLocation tempBaseLocation;
			TilePosition tempTilePosition = null;
			Region tempBaseRegion;
			int vx, vy;
			double d, t;
			int bx, by;

			switch (seedPositionStrategy) {

			case MainBaseLocation:
				desiredPosition = getBuildLocationNear(buildingType, InformationManager.Instance().getMainBaseLocation(MyBotModule.Broodwar.self()).getTilePosition());
				break;

			case MainBaseBackYard:
				tempBaseLocation = InformationManager.Instance().getMainBaseLocation(MyBotModule.Broodwar.self());
				tempChokePoint = InformationManager.Instance().getFirstChokePoint(MyBotModule.Broodwar.self());
				tempBaseRegion = BWTA.getRegion(tempBaseLocation.getPosition());

				//std::cout << "y";

				// (vx, vy) = BaseLocation �� ChokePoint �� ���� ���� = �Ÿ� d �� ���� t ����. ������ position
				// ��Ÿũ����Ʈ ��ǥ�� : ���������� ������ x �� ���� (��ī��Ʈ ��ǥ��� ����). �Ʒ��� ������ y�� ���� (y�ุ ��ī��Ʈ ��ǥ��� �ݴ�)
				// �ﰢ�Լ� ���� ��ī��Ʈ ��ǥ�迡�� ����ϹǷ�, vy�� ��ȣ �ݴ�� �ؼ� ���� t ���� ���� 

				// MainBaseLocation �� null �̰ų�, ChokePoint �� null �̸�, MainBaseLocation �������� ������ ���� �����Ѵ�
				if (tempBaseLocation == null ) {
					//std::cout << "q";
					desiredPosition = getBuildLocationNear(buildingType, InformationManager.Instance().getMainBaseLocation(MyBotModule.Broodwar.self()).getTilePosition());
					break;
				}
				else if (tempChokePoint == null) {
					//std::cout << "r";
					desiredPosition = getBuildLocationNear(buildingType, InformationManager.Instance().getMainBaseLocation(MyBotModule.Broodwar.self()).getTilePosition());
					break;
				}

				// BaseLocation ���� ChokePoint ���� ���͸� ���Ѵ�
				vx = tempChokePoint.getCenter().getX() - tempBaseLocation.getPosition().getX();
				//std::cout << "vx : " << vx ;
				vy = (tempChokePoint.getCenter().getY() - tempBaseLocation.getPosition().getY()) * (-1);
				//std::cout << "vy : " << vy;
				d = Math.sqrt(vx * vx + vy * vy) * 0.5; // BaseLocation �� ChokePoint �� �Ÿ����� ���� ª�� �Ÿ��� ����. BaseLocation�� �ִ� Region�� ��κ� ���簢�� �����̱� ����
				//std::cout << "d : " << d;
				t = Math.atan2(vy, vx + 0.0001); // ���� ����
				//std::cout << "t : " << t;

				// cos(t+90), sin(t+180) �� �ﰢ�Լ� Trigonometric functions of allied angles �� �̿�. y�࿡ ���ؼ��� �ݴ��ȣ�� ����

				// BaseLocation ���� ChokePoint �ݴ��� ������ Back Yard : ��ī��Ʈ ��ǥ�迡�� (cos(t+180) = -cos(t), sin(t+180) = -sin(t))
				bx = tempBaseLocation.getTilePosition().getX() - (int)(d * Math.cos(t) / Config.TILE_SIZE);
				by = tempBaseLocation.getTilePosition().getY() + (int)(d * Math.sin(t) / Config.TILE_SIZE);
				//std::cout << "i";
				tempTilePosition = new TilePosition(bx, by);
				//if (Config::Debug::LogToConsole) std::cout << "ConstructionPlaceFinder MainBaseBackYard tempTilePosition " << tempTilePosition.x << "," << tempTilePosition.y << std::endl;
				
				//std::cout << "k";
				// �ش� ������ ���� Region �� ���ϰ� Buildable �� Ÿ������ Ȯ��
				if (!tempTilePosition.isValid() || !MyBotModule.Broodwar.isBuildable(tempTilePosition.getX(), tempTilePosition.getY(), false) || tempBaseRegion != BWTA.getRegion(new Position(bx*Config.TILE_SIZE, by*Config.TILE_SIZE))) {
					//std::cout << "l";

					// BaseLocation ���� ChokePoint ���⿡ ���� ���������� 90�� ���� ������ Back Yard : ��ī��Ʈ ��ǥ�迡�� (cos(t-90) = sin(t),   sin(t-90) = - cos(t))
					bx = tempBaseLocation.getTilePosition().getX() + (int)(d * Math.sin(t) / Config.TILE_SIZE);
					by = tempBaseLocation.getTilePosition().getY() + (int)(d * Math.cos(t) / Config.TILE_SIZE);
					tempTilePosition = new TilePosition(bx, by);
					//if (Config::Debug::LogToConsole) std::cout << "ConstructionPlaceFinder MainBaseBackYard tempTilePosition " << tempTilePosition.x << "," << tempTilePosition.y << std::endl;
					//std::cout << "m";

					if (!tempTilePosition.isValid() || !MyBotModule.Broodwar.isBuildable(tempTilePosition.getX(), tempTilePosition.getY(), false)) {
						// BaseLocation ���� ChokePoint ���⿡ ���� �������� 90�� ���� ������ Back Yard : ��ī��Ʈ ��ǥ�迡�� (cos(t+90) = -sin(t),   sin(t+90) = cos(t))
						bx = tempBaseLocation.getTilePosition().getX() - (int)(d * Math.sin(t) / Config.TILE_SIZE);
						by = tempBaseLocation.getTilePosition().getY() - (int)(d * Math.cos(t) / Config.TILE_SIZE);
						tempTilePosition = new TilePosition(bx, by);
						//if (Config::Debug::LogToConsole) std::cout << "ConstructionPlaceFinder MainBaseBackYard tempTilePosition " << tempTilePosition.x << "," << tempTilePosition.y << std::endl;

						if (!tempTilePosition.isValid() || !MyBotModule.Broodwar.isBuildable(tempTilePosition.getX(), tempTilePosition.getY(), false) || tempBaseRegion != BWTA.getRegion(new Position(bx*Config.TILE_SIZE, by*Config.TILE_SIZE))) {

							// BaseLocation ���� ChokePoint ���� ���� ������ Back Yard : ��ī��Ʈ ��ǥ�迡�� (cos(t),   sin(t))
							bx = tempBaseLocation.getTilePosition().getX() + (int)(d * Math.cos(t) / Config.TILE_SIZE);
							by = tempBaseLocation.getTilePosition().getY() - (int)(d * Math.sin(t) / Config.TILE_SIZE);
							tempTilePosition = new TilePosition(bx, by);
							//if (Config::Debug::LogToConsole) std::cout << "ConstructionPlaceFinder MainBaseBackYard tempTilePosition " << tempTilePosition.x << "," << tempTilePosition.y << std::endl;
							//std::cout << "m";
						}

					}
				}
				//std::cout << "z";
				if (!tempTilePosition.isValid() || !MyBotModule.Broodwar.isBuildable(tempTilePosition.getX(), tempTilePosition.getY(), false)) {
					desiredPosition = getBuildLocationNear(buildingType, tempBaseLocation.getTilePosition());
				}
				else {
					desiredPosition = getBuildLocationNear(buildingType, tempTilePosition);
				}
				//std::cout << "w";
				//if (Config::Debug::LogToConsole) std::cout << "ConstructionPlaceFinder MainBaseBackYard desiredPosition " << desiredPosition.x << "," << desiredPosition.y << std::endl;
				break;

			case FirstExpansionLocation:
				tempBaseLocation = InformationManager.Instance().getFirstExpansionLocation(MyBotModule.Broodwar.self());
				if (tempBaseLocation != null) {
					desiredPosition = getBuildLocationNear(buildingType, tempBaseLocation.getTilePosition());
				}
				break;

			case FirstChokePoint:
				tempChokePoint = InformationManager.Instance().getFirstChokePoint(MyBotModule.Broodwar.self());
				if (tempChokePoint != null) {
					desiredPosition = getBuildLocationNear(buildingType, tempChokePoint.getCenter().toTilePosition());
				}
				break;

			case SecondChokePoint:
				tempChokePoint = InformationManager.Instance().getSecondChokePoint(MyBotModule.Broodwar.self());
				if (tempChokePoint != null) {
					desiredPosition = getBuildLocationNear(buildingType, tempChokePoint.getCenter().toTilePosition());
				}
				break;

			case SecondExpansionLocation:
				if (InformationManager.Instance().getOccupiedBaseLocations(MyBotModule.Broodwar.self()).size() > 1) {
					for (BaseLocation baseLocation : InformationManager.Instance().getOccupiedBaseLocations(MyBotModule.Broodwar.self())) {

						if (baseLocation != InformationManager.Instance().getMainBaseLocation(MyBotModule.Broodwar.self())
							&& baseLocation != InformationManager.Instance().getFirstExpansionLocation(MyBotModule.Broodwar.self()))
						{
							tempTilePosition = baseLocation.getTilePosition();
						}
					}
					if (tempTilePosition != TilePosition.None) {
						desiredPosition = tempTilePosition;
					}
				}
				break;

			}
		}

		return desiredPosition;
	}

	/// desiredPosition ��ó���� �ǹ� �Ǽ� ���� ��ġ�� Ž���ؼ� �����մϴ�
	/// desiredPosition �������� ������ ���� ã�� ��ȯ�մϴ�
	/// desiredPosition �� valid �� ���� �ƴ϶��, desiredPosition �� MainBaseLocation �� �ؼ� ������ ã�´�
	/// Returns a suitable TilePosition to build a given building type near specified TilePosition aroundTile.
	/// Returns BWAPI::TilePositions::None, if suitable TilePosition is not exists (�ٸ� ���ֵ��� �ڸ��� �־, Pylon, Creep, �ǹ����� Ÿ�� ������ ���� ���� ��� ��)
	public final TilePosition getBuildLocationNear(UnitType buildingType, TilePosition desiredPosition)
	{
		if (buildingType.isRefinery())
		{
			//std::cout << "getRefineryPositionNear "<< std::endl;

			return getRefineryPositionNear(desiredPosition);
		}

		if (MyBotModule.Broodwar.self().getRace() == Race.Protoss) {
			// special easy case of having no pylons
			if (buildingType.requiresPsi() && MyBotModule.Broodwar.self().completedUnitCount(UnitType.Protoss_Pylon) == 0)
			{
				return TilePosition.None;
			}
		}

		if (desiredPosition == TilePosition.None || desiredPosition == TilePosition.Unknown || desiredPosition == TilePosition.Invalid || desiredPosition.isValid() == false)
		{
			desiredPosition = InformationManager.Instance().getMainBaseLocation(MyBotModule.Broodwar.self()).getTilePosition();
		}

		TilePosition testPosition = TilePosition.None;

		// TODO ���� : �Ǽ� ��ġ Ž�� ����� ConstructionPlaceSearchMethod::SpiralMethod �� �ϴµ�, �� ���� ����� �����غ� �����̴�
		int constructionPlaceSearchMethod = ConstructionPlaceSearchMethod.SpiralMethod.ordinal();
		
		// �Ϲ����� �ǹ��� ���ؼ��� �ǹ� ũ�⺸�� Config::Macro::BuildingSpacing ĭ ��ŭ �����¿�� �� �а� ���������� �ξ �� �ڸ��� �˻��Ѵ�
		int buildingGapSpace = Config.BuildingSpacing;

		// ResourceDepot (Nexus, Command Center, Hatchery),
		// Protoss_Pylon, Terran_Supply_Depot, 
		// Protoss_Photon_Cannon, Terran_Bunker, Terran_Missile_Turret, Zerg_Creep_Colony �� �ٸ� �ǹ� �ٷ� ���� �ٿ� ���� ��찡 �����Ƿ� 
		// buildingGapSpace�� �ٸ� Config ������ �����ϵ��� �Ѵ�
		if (buildingType.isResourceDepot()) {
			buildingGapSpace = Config.BuildingResourceDepotSpacing;		
		}
		else if (buildingType == UnitType.Protoss_Pylon) {
			int numPylons = MyBotModule.Broodwar.self().completedUnitCount(UnitType.Protoss_Pylon);
			
			// Protoss_Pylon �� Ư�� ���� 2�� �Ǽ��Ҷ��� Config::Macro::BuildingPylonEarlyStageSpacing ������ �����Ѵ�
			if (numPylons < 3) {
				buildingGapSpace = Config.BuildingPylonEarlyStageSpacing;
			}
			else {
				buildingGapSpace = Config.BuildingPylonSpacing;
			}
		}
		else if (buildingType == UnitType.Terran_Supply_Depot) {
			buildingGapSpace = Config.BuildingSupplyDepotSpacing;
		}
		else if (buildingType == UnitType.Protoss_Photon_Cannon || buildingType == UnitType.Terran_Bunker 
			|| buildingType == UnitType.Terran_Missile_Turret || buildingType == UnitType.Zerg_Creep_Colony) {
			buildingGapSpace = Config.BuildingDefenseTowerSpacing;
		}

		while (buildingGapSpace >= 0) {

			testPosition = getBuildLocationNear(buildingType, desiredPosition, buildingGapSpace, constructionPlaceSearchMethod);

			//if (Config::Debug::LogToConsole) std::cout << "ConstructionPlaceFinder testPosition " << testPosition.x << "," << testPosition.y << std::endl;

			if (testPosition != TilePosition.None && testPosition != TilePosition.Invalid)
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

		return TilePosition.None;
	}

	/// �ش� buildingType �� �Ǽ��� �� �ִ� ��ġ�� desiredPosition ��ó���� Ž���ؼ� Ž������� �����մϴ�
	/// buildingGapSpace�� �ݿ��ؼ� canBuildHereWithSpace �� ����ؼ� üũ
	/// ��ã�´ٸ� BWAPI::TilePositions::None �� �����մϴ�
	/// TODO ���� : �ǹ��� ��ȹ���� ������ �ִ� ���� ���� ���� ��� �ϴٺ���, ������ �ǹ� ���̿� ������ ��찡 �߻��� �� �ִµ�, �̸� �����ϴ� ����� �����غ� �����Դϴ�
	public final TilePosition getBuildLocationNear(UnitType buildingType, TilePosition desiredPosition, int buildingGapSpace, int constructionPlaceSearchMethod)
	{
		//if (Config::Debug::LogToConsole) std::cout << std::endl << "getBuildLocationNear " << buildingType.getName().c_str() << " " << desiredPosition.x << "," << desiredPosition.y 
		//	<< " gap " << buildingGapSpace << " method " << constructionPlaceSearchMethod << std::endl;

		//returns a valid build location near the desired tile position (x,y).
		TilePosition resultPosition = TilePosition.None;
		TilePosition tempPosition;
		ConstructionTask b = new ConstructionTask(buildingType, desiredPosition);

		// maxRange �� �������� �ʰų�, maxRange �� 128���� �����ϸ� ���� ��ü�� �� Ž���ϴµ�, �ſ� �������Ӹ� �ƴ϶�, ��κ��� ��� ���ʿ��� Ž���� �ȴ�
		// maxRange �� 16 ~ 64�� �����ϴ�
		int maxRange = 32; // maxRange = BWAPI::Broodwar->mapWidth()/4;
		boolean isPossiblePlace = false;
			
		if (constructionPlaceSearchMethod == ConstructionPlaceSearchMethod.SpiralMethod.ordinal())
		{
			// desiredPosition ���κ��� �����ؼ� spiral �ϰ� (�Ʒ�->������->��->����->�Ʒ�->..) Ž���ϴ� ���.by AIUR
			// ������ ���.
			//searches outward in a spiral.
			int x = desiredPosition.getX();
			int y = desiredPosition.getY();
			int length = 1;
			int j = 0;
			boolean first = true;

			// ó������ �Ʒ� ����
			int dx = 0;
			int dy = 1;
			while (length < maxRange) //We'll ride the spiral to the end
			{
				// if we can build here, return this tile position
				if (x >= 0 && x < MyBotModule.Broodwar.mapWidth() && y >= 0 && y < MyBotModule.Broodwar.mapHeight()) {

					isPossiblePlace = canBuildHereWithSpace(new TilePosition(x, y), b, buildingGapSpace);

					//if (Config::Debug::LogToConsole) std::cout << " " << x << "," << y << "=" << isPossiblePlace;

					if (isPossiblePlace) {
						resultPosition = new TilePosition(x, y);
						break;
					}
				}

				//otherwise, move to another position
				x = x + dx;
				y = y + dy;
				//count how many steps we take in this direction
				j++;
				if (j == length) //if we've reached the end, its time to turn
				{
					//reset step counter
					j = 0;

					//Spiral out. Keep going.
					if (!first)
						length++; //increment step counter if needed

					//first=true for every other turn so we spiral out at the right rate
					first = !first;

					//turn counter clockwise 90 degrees:
					// �Ʒ���(0,1) -> ����������(1,0) -> ����(0,-1) -> ��������(-1,0) -> �Ʒ���(0,1) -> ..
					if (dx == 0)
					{
						dx = dy;
						dy = 0;
					}
					else
					{
						dy = -dx;
						dx = 0;
					}
				}
				//Spiral out. Keep going.
			}
		}
		/*
		else if (constructionPlaceSearchMethod == ConstructionPlaceSearchMethod::BWAPIMethod) {
			
			// ����1.BWAPI �����ڰ� �������� getBuildLocation �Լ��� ����ؼ� ã�� ���
			// BWAPI::Broodwar->getBuildLocation(buildingType, desiredPosition, maxRange, buildingType == BWAPI::UnitTypes::Zerg_Creep_Colony)
			// If BWAPI::Broodwar->self() is nullptr, this will crash
			// �ǹ� ���� �� �ִ� ������ �ִµ� ��ã�� ��찡 �����
			// ResourceDepot ���� �� ��ġ�� �����ϴ� ������ �־, ��Ƽ ���� �� �� ��Ƽ �Ǽ��� �����ʴ�. 
			maxRange = 16;
			for (; maxRange <= 64; maxRange = maxRange * 2) {

				tempPosition = BWAPI::Broodwar->getBuildLocation(buildingType, desiredPosition, maxRange, buildingType == BWAPI::UnitTypes::Zerg_Creep_Colony);

				if (tempPosition != BWAPI::TilePositions::Invalid && tempPosition != BWAPI::TilePositions::None) {
					isPossiblePlace = canBuildHereWithSpace(tempPosition, b, buildingGapSpace);
					if (isPossiblePlace) {
						resultPosition = tempPosition;
						break;
					}
				}
			}

		}
		else if (constructionPlaceSearchMethod == ConstructionPlaceSearchMethod::DistanceMapMethod) {
			// ����2. uAlbertaBot �� DistanceMap �� ����ϴ� ���
			// const std::vector<BWAPI::TilePosition> & closestTiles = MapTools::Instance().getClosestTilesTo(BWAPI::Position(desiredPosition));
			// for (size_t i(0); i < closestTiles.size(); ++i) isPossiblePlace = canBuildHereWithSpace(closestTiles[i], b, buildingGapSpace);
			// desiredPosition ���κ��� ����� Ÿ�ϵ鿡 ���� üũ�غ��� ã�� ���. by uAlbertaBot 
			// get the precomputed vector of tile positions which are sorted closes to this location
			// �ǹ� ���� �� �ִ� ������ �ִµ� ��ã�� ��찡 �����
			const std::vector<BWAPI::TilePosition> & closestTiles = MapTools::Instance().getClosestTilesTo(BWAPI::Position(desiredPosition));

			// iterate through the list until we've found a suitable location
			for (size_t i(0); i < closestTiles.size(); ++i)
			{
				isPossiblePlace = canBuildHereWithSpace(closestTiles[i], b, buildingGapSpace);

				//if (Config::Debug::LogToConsole) std::cout << " " << closestTiles[i].x << "," << closestTiles[i].y << "=" << isPossiblePlace;

				if (isPossiblePlace)
				{
					resultPosition = closestTiles[i];
					break;
				}
			}
		}
		*/
		else if (constructionPlaceSearchMethod == ConstructionPlaceSearchMethod.NewMethod.ordinal()) {
		}

		return resultPosition;
	}

	/// �ش� ��ġ�� �ǹ� �Ǽ��� �������� ���θ� buildingGapSpace ������ �����ؼ� �Ǵ��Ͽ� �����մϴ�
	/// Broodwar �� canBuildHere, isBuildableTile, isReservedTile �� üũ�մϴ�
	public final boolean canBuildHereWithSpace(TilePosition position, final ConstructionTask b, int buildingGapSpace)
	{
		//if we can't build here, we of course can't build here with space
		if (!canBuildHere(position, b))
		{
			return false;
		}

		// height and width of the building
		int width = b.getType().tileWidth();
		int height = b.getType().tileHeight();

		// define the rectangle of the building spot
		// �ǹ� ũ�⺸�� �����¿�� �� ū �簢��
		int startx;
		int starty;
		int endx;
		int endy;

		boolean horizontalOnly = false;

		// Refinery �� ��� GapSpace�� üũ�� �ʿ� ����
		if (b.getType().isRefinery())
		{
		}
		// Addon Ÿ���� �ǹ��� ��쿡��, �� Addon �ǹ� ���ʿ� whatBuilds �ǹ��� �ִ����� üũ�Ѵ�
		if (b.getType().isAddon())
		{
			final UnitType builderType = b.getType().whatBuilds().first;

			TilePosition builderTile = new TilePosition(position.getX() - builderType.tileWidth(), position.getY() + 2 - builderType.tileHeight());

			startx = builderTile.getX() - buildingGapSpace;
			starty = builderTile.getY() - buildingGapSpace;
			endx = position.getX() + width + buildingGapSpace;
			endy = position.getY() + height + buildingGapSpace;

			// builderTile�� Lifted �ǹ��� �ƴϰ� whatBuilds �ǹ��� �ƴ� �ǹ��� �ִ��� üũ
			for (int i = 0; i <= builderType.tileWidth(); ++i)
			{
				for (int j = 0; j <= builderType.tileHeight(); ++j)
				{
					for (Unit unit : MyBotModule.Broodwar.getUnitsOnTile(builderTile.getX() + i, builderTile.getY() + j))
					{
						if ((unit.getType() != builderType) && (!unit.isLifted()))
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
			if (b.getType() == UnitType.Terran_Command_Center ||
				b.getType() == UnitType.Terran_Factory ||
				b.getType() == UnitType.Terran_Starport ||
				b.getType() == UnitType.Terran_Science_Facility)
			{
				width += 2;
			}

			// �����¿쿡 buildingGapSpace ��ŭ ������ ����
			if (horizontalOnly == false)
			{
				startx = position.getX() - buildingGapSpace;
				starty = position.getY() - buildingGapSpace;
				endx = position.getX() + width + buildingGapSpace;
				endy = position.getY() + height + buildingGapSpace;
			}
			// �¿�θ� buildingGapSpace ��ŭ ������ ����
			else {
				startx = position.getX() - buildingGapSpace;
				starty = position.getY();
				endx = position.getX() + width + buildingGapSpace;
				endy = position.getY() + height;
			}

			// �׶����� �ǹ��� ��� �ٸ� �ǹ��� Addon ������ Ȯ�����ֱ� ����, ���� 2ĭ�� �ݵ�� GapSpace�� �ǵ��� �Ѵ�
			if (b.getType().getRace() == Race.Terran) {
				if (buildingGapSpace < 2) {
					startx = position.getX() - 2;
					endx = position.getX() + width + buildingGapSpace;
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
					if (b.getType().isResourceDepot() == false && b.getType().isAddon() == false) {
						if (isTilesToAvoid(x, y)) {
							return false;
						}
					}
				}
			}
		}

		// if this rectangle doesn't fit on the map we can't build here
		if (startx < 0 || starty < 0 || endx > MyBotModule.Broodwar.mapWidth() || endx < position.getX() + width || endy > MyBotModule.Broodwar.mapHeight())
		{
			return false;
		}

		return true;
	}

	/// �ش� ��ġ�� �ǹ� �Ǽ��� �������� ���θ� �����մϴ�
	// checks to see if a building can be built at a certain location
	// Broodwar �� canBuildHere �� _reserveMap �� isOverlapsWithBaseLocation �� üũ
	public final boolean canBuildHere(TilePosition position, final ConstructionTask b)
	{
		/*if (!b.type.isRefinery() && !InformationManager::Instance().tileContainsUnit(position))
		{
		return false;
		}*/
		
		// This function checks for creep, power, and resource distance requirements in addition to the tiles' buildability and possible units obstructing the build location.
//		if (!MyBotModule.Broodwar.canBuildHere(position, b.getType(), b.getConstructionWorker()))
		if (!MyBotModule.Broodwar.canBuildHere(position, b.getType()))
		{
			return false;
		}
		
		// check the reserve map
		for (int x = position.getX() ; x < position.getX() + b.getType().tileWidth(); x++)
		{
			for (int y = position.getY() ; y < position.getY() + b.getType().tileHeight(); y++)
			{
				//if (reserveMap.get(x).get(y))
				if (reserveMap[x][y])
				{
					return false;
				}
			}
		}

		// if it overlaps a base location return false
		// ResourceDepot �ǹ��� �ƴ� �ٸ� �ǹ��� BaseLocation ��ġ�� ���� ���ϵ��� �Ѵ�
		if (isOverlapsWithBaseLocation(position, b.getType()))
		{
			return false;
		}

		return true;
	}

	/// seedPosition ��ó���� Refinery �ǹ� �Ǽ� ���� ��ġ�� Ž���ؼ� �����մϴ�
	/// �������� ���� ���� ���� (Resource_Vespene_Geyser) �� ����Ǿ����� ���� ��(isReservedTile), �ٸ� ���� �ƴ� ��, �̹� Refinery �� �������������� �� �� 
	/// seedPosition �� ���� ����� ���� �����մϴ�
	public final TilePosition getRefineryPositionNear(TilePosition seedPosition)
	{
		if (seedPosition == TilePosition.None || seedPosition == TilePosition.Unknown || seedPosition == TilePosition.Invalid || seedPosition.isValid() == false)
		{
			seedPosition = InformationManager.Instance().getMainBaseLocation(MyBotModule.Broodwar.self()).getTilePosition();
		}
		
		TilePosition closestGeyser = TilePosition.None;
		double minGeyserDistanceFromSeedPosition = 100000000;

		// for each geyser
		for (Unit geyser : MyBotModule.Broodwar.getStaticGeysers())
		{
			// geyser->getType() �� �ϸ�, Unknown �̰ų�, Resource_Vespene_Geyser �̰ų�, Terran_Refinery �� ���� �ǹ����� ������, 
			// �ǹ��� �ı��Ǿ �ڵ����� Resource_Vespene_Geyser �� ���ư��� �ʴ´�

			Position geyserPos = geyser.getInitialPosition();
			TilePosition geyserTilePos = geyser.getInitialTilePosition();

			//std::cout << " geyserTilePos " << geyserTilePos.x << "," << geyserTilePos.y << std::endl;

			// �̹� ����Ǿ��ִ°�
			if (isReservedTile(geyserTilePos.getX(), geyserTilePos.getY())) {
				continue;
			}

			// if it is not connected fron seedPosition, it is located in another island
			if (!BWTA.isConnected(seedPosition, geyserTilePos))
			{
				continue;
			}

			// �̹� ������ �ִ°�
			boolean refineryAlreadyBuilt = false;
			// ����
			List<Unit> alreadyBuiltUnits = MyBotModule.Broodwar.getUnitsInRadius(geyserPos, 4 * Config.TILE_SIZE);

			for (Unit u : alreadyBuiltUnits) {
				if (u.getPlayer() == MyBotModule.Broodwar.self() 
					&& u.getType().isRefinery() && u.exists()) {
					refineryAlreadyBuilt = true;
				}
			}

			//std::cout << " geyser TilePos is not reserved, is connected, is not refineryAlreadyBuilt" << std::endl;

			if (refineryAlreadyBuilt == false)
			{
				double thisDistance = BWTA.getGroundDistance(geyserPos.toTilePosition(), seedPosition);

				if (thisDistance < minGeyserDistanceFromSeedPosition)
				{
					//std::cout << " selected " << std::endl;

					minGeyserDistanceFromSeedPosition = thisDistance;
					closestGeyser = geyser.getInitialTilePosition();
				}
			}
		}
		return closestGeyser;
	}

	/// �ش� ��ġ�� BaseLocation �� ��ġ���� ���θ� �����մϴ�
	/// BaseLocation ���� ResourceDepot �ǹ��� �Ǽ��ϰ�, �ٸ� �ǹ��� �Ǽ����� �ʱ� �����Դϴ�
	public final boolean isOverlapsWithBaseLocation(TilePosition tile, UnitType type)
	{
		// if it's a resource depot we don't care if it overlaps
		if (type.isResourceDepot())
		{
			return false;
		}

		// dimensions of the proposed location
		int tx1 = tile.getX();
		int ty1 = tile.getY();
		int tx2 = tx1 + type.tileWidth();
		int ty2 = ty1 + type.tileHeight();

		// for each base location
		for (BaseLocation base : BWTA.getBaseLocations())
		{
			// dimensions of the base location
			int bx1 = base.getTilePosition().getX();
			int by1 = base.getTilePosition().getY();
			int bx2 = bx1 + InformationManager.Instance().getBasicResourceDepotBuildingType().tileWidth();
			int by2 = by1 + InformationManager.Instance().getBasicResourceDepotBuildingType().tileHeight();

			// conditions for non-overlap are easy
			boolean noOverlap = (tx2 < bx1) || (tx1 > bx2) || (ty2 < by1) || (ty1 > by2);

			// if the reverse is true, return true
			if (!noOverlap)
			{
				return true;
			}
		}

		// otherwise there is no overlap
		return false;
	}

	/// �ǹ� �Ǽ� ���� Ÿ������ ���θ� �����մϴ�
	public final boolean isBuildableTile(final ConstructionTask b, int x, int y)
	{
		TilePosition tp = new TilePosition(x, y);
		if (!tp.isValid())
		{
			return false;
		}

		// �� ������ �Ӹ� �ƴ϶� ���� �����͸� ��� ����ؼ� isBuildable üũ
		//if (BWAPI::Broodwar->isBuildable(x, y) == false)
		if (MyBotModule.Broodwar.isBuildable(x, y, true) == false)
		{
			return false;
		}

		// constructionWorker �̿��� �ٸ� ������ ������ false�� �����Ѵ�
		for (Unit unit : MyBotModule.Broodwar.getUnitsOnTile(x, y))
		{
			if ((b.getConstructionWorker() != null) && (unit != b.getConstructionWorker()))
			{
				return false;
			}
		}

		return true;
	}

	/// �ǹ� �Ǽ� ���� Ÿ�Ϸ� �����ؼ�, �ٸ� �ǹ��� �ߺ��ؼ� ���� �ʵ��� �մϴ�
	public void reserveTiles(TilePosition position, int width, int height)
	{
		/*int rwidth = reserveMap.size();
		int rheight = reserveMap.get(0).size();
		for (int x = position.getX(); x < position.getX() + width && x < rwidth; x++)
		{
			for (int y = position.getY() ; y < position.getY() + height && y < rheight; y++)
			{
				reserveMap.get(x).set(y, true);
				// C++ : reserveMap[x][y] = true;
			}
		}*/
		int rwidth = reserveMap.length;
		int rheight = reserveMap[0].length;
		for (int x = position.getX(); x < position.getX() + width && x < rwidth; x++)
		{
			for (int y = position.getY() ; y < position.getY() + height && y < rheight; y++)
			{
				//reserveMap.get(x).set(y, true);
				reserveMap[x][y] = true;
				// C++ : reserveMap[x][y] = true;
			}
		}
	}
	
	/// �ǹ� �Ǽ� ���� Ÿ�Ϸ� �����ߴ� ���� �����մϴ�
	public void freeTiles(TilePosition position, int width, int height)
	{
		/*int rwidth = reserveMap.size();
		int rheight = reserveMap.get(0).size();

		for (int x = position.getX(); x < position.getX() + width && x < rwidth; x++)
		{
			for (int y = position.getY() ; y < position.getY() + height && y < rheight; y++)
			{
				reserveMap.get(x).set(y, false);
				// C++ : reserveMap[x][y] = false;
			}
		}*/
		int rwidth = reserveMap.length;
		int rheight = reserveMap[0].length;

		for (int x = position.getX(); x < position.getX() + width && x < rwidth; x++)
		{
			for (int y = position.getY() ; y < position.getY() + height && y < rheight; y++)
			{
				//reserveMap.get(x).set(y, false);
				reserveMap[x][y] = false;
				// C++ : reserveMap[x][y] = false;
			}
		}
	}

	// �ǹ� �Ǽ� ����Ǿ��ִ� Ÿ������ üũ
	public final boolean isReservedTile(int x, int y)
	{
		/*int rwidth = reserveMap.size();
		int rheight = reserveMap.get(0).size();
		if (x < 0 || y < 0 || x >= rwidth || y >= rheight)
		{
			return false;
		}

		return reserveMap.get(x).get(y);*/
		int rwidth = reserveMap.length;
		int rheight = reserveMap[0].length;
		if (x < 0 || y < 0 || x >= rwidth || y >= rheight)
		{
			return false;
		}

		return reserveMap[x][y];
	}

	/// reserveMap�� �����մϴ�
	public boolean[][] getReserveMap() {
		return reserveMap;
	}

	/// (x, y) �� BaseLocation �� Mineral / Geyser ������ Ÿ�Ͽ� �ش��ϴ��� ���θ� �����մϴ�
	public final boolean isTilesToAvoid(int x, int y)
	{
		for (TilePosition t : tilesToAvoid) {
			if (t.getX() == x && t.getY() == y) {
				return true;
			}
		}

		return false;
	}

	/// BaseLocation �� Mineral / Geyser ������ Ÿ�ϵ��� ã�� _tilesToAvoid �� �����մϴ�
	/// BaseLocation �� Geyser ����, ResourceDepot �ǹ��� Mineral ���� �������� �ǹ� �Ǽ� ��Ҹ� ���ϸ� 
	/// �ϲ� ���ֵ��� ��ֹ��� �Ǿ �Ǽ� ���۵Ǳ���� �ð��� �����ɸ���, ������ �ǹ��� ��ֹ��� �Ǿ �ڿ� ä�� �ӵ��� �������� ������, �� ������ �ǹ��� ���� �ʴ� �������� �α� �����Դϴ�
	public void setTilesToAvoid()
	{
		// ResourceDepot �ǹ��� width = 4 Ÿ��, height = 3 Ÿ��
		// Geyser ��            width = 4 Ÿ��, height = 2 Ÿ��
		// Mineral ��           width = 2 Ÿ��, height = 1 Ÿ��

		for (BaseLocation base : BWTA.getBaseLocations())
		{
			// Island �� ��� �ǹ� ���� ������ ���������� ���� ������ �ǹ� ������ ������ ���� �ʴ´�
			if (base.isIsland()) continue;
			if (BWTA.isConnected(base.getTilePosition(), InformationManager.Instance().getMainBaseLocation(MyBotModule.Broodwar.self()).getTilePosition()) == false) continue;

			// dimensions of the base location
			int bx0 = base.getTilePosition().getX();
			int by0 = base.getTilePosition().getY();
			int bx4 = base.getTilePosition().getX() + 4;
			int by3 = base.getTilePosition().getY() + 3;

			// BaseLocation �� Geyser ������ Ÿ���� BWTA::getShortestPath �� ����ؼ� ���� �� _tilesToAvoid �� �߰�
			for (Unit geyser : base.getGeysers())
			{
				TilePosition closeGeyserPosition = geyser.getInitialTilePosition();

				// dimensions of the closest geyser
				int gx0 = closeGeyserPosition.getX();
				int gy0 = closeGeyserPosition.getY();
				int gx4 = closeGeyserPosition.getX() + 4;
				int gy2 = closeGeyserPosition.getY() + 2;

				for (int i = bx0; i < bx4; i++) {
					for (int j = by0; j < by3; j++) {
						for (int k = gx0; k < gx4; k++) {
							for (int l = gy0; l < gy2; l++) {
								List<TilePosition> tileList = (List<TilePosition>) BWTA.getShortestPath(new TilePosition(i, j), new TilePosition(k, l));
								for (TilePosition t : tileList) {
									tilesToAvoid.add(t);									
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
			for (Unit mineral : base.getMinerals())
			{
				TilePosition closeMineralPosition = mineral.getInitialTilePosition();

				// dimensions of the closest mineral
				int mx0 = closeMineralPosition.getX();
				int my0 = closeMineralPosition.getY();
				int mx2 = mx0 + 2;
				int my1 = my0 + 1;

				for (int i = bx0; i < bx4; i++) {
					for (int j = by0; j < by3; j++) {
						for (int k = mx0; k < mx2; k++) {
							List<TilePosition> tileList = (List<TilePosition>) BWTA.getShortestPath(new TilePosition(i, j), new TilePosition(k, my0));
							for (TilePosition t : tileList) {
								tilesToAvoid.add(t);								
							}
						}
					}
				}
			}
		}
	}
	
	/// BaseLocation �� Mineral / Geyser ������ Ÿ�ϵ��� ����� �����մϴ�		
	public Set<TilePosition> getTilesToAvoid() {
		return tilesToAvoid;
	}
	
}