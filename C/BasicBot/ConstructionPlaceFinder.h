#pragma once

#include "Common.h"
#include "MapTools.h"
#include "ConstructionTask.h"
#include "MetaType.h"
#include "BuildOrderQueue.h"
#include "InformationManager.h"

namespace MyBot
{
	/// �Ǽ���ġ Ž�� ���
	namespace ConstructionPlaceSearchMethod
	{
		enum { 
			SpiralMethod = 0,	///< ���������� �� �а� Ȯ���ϸ� Ž��
			NewMethod = 1		///< ����
		};
	}

	/// �Ǽ���ġ Ž���� ���� class
	class ConstructionPlaceFinder
	{
		ConstructionPlaceFinder();

		/// �ǹ� �Ǽ� ���� Ÿ���� �����س��� ���� 2���� �迭
		/// TilePosition �����̱� ������ ���� 128*128 ����� �ȴ�
		/// �����, �ǹ��� �̹� ������ Ÿ���� �������� �ʴ´�
		std::vector< std::vector<bool> > _reserveMap;

		/// BaseLocation �� Mineral / Geyser ������ Ÿ�ϵ��� ��� �ڷᱸ��. ���⿡�� Addon �̿ܿ��� �ǹ��� ���� �ʵ��� �մϴ�
		std::set< BWAPI::TilePosition > _tilesToAvoid;

		/// BaseLocation �� Mineral / Geyser ������ Ÿ�ϵ��� ã�� _tilesToAvoid �� �����մϴ�
		/// BaseLocation �� Geyser ����, ResourceDepot �ǹ��� Mineral ���� �������� �ǹ� �Ǽ� ��Ҹ� ���ϸ� 
		/// �ϲ� ���ֵ��� ��ֹ��� �Ǿ �Ǽ� ���۵Ǳ���� �ð��� �����ɸ���, ������ �ǹ��� ��ֹ��� �Ǿ �ڿ� ä�� �ӵ��� �������� ������, �� ������ �ǹ��� ���� �ʴ� �������� �α� �����Դϴ�
		void					setTilesToAvoid();
		
		/// �ش� buildingType �� �Ǽ��� �� �ִ� ��ġ�� desiredPosition ��ó���� Ž���ؼ� Ž������� �����մϴ�
		/// buildingGapSpace�� �ݿ��ؼ� canBuildHereWithSpace �� ����ؼ� üũ
		/// ��ã�´ٸ� BWAPI::TilePositions::None �� �����մϴ�
		/// TODO ���� : �ǹ��� ��ȹ���� ������ �ִ� ���� ���� ���� ��� �ϴٺ���, ������ �ǹ� ���̿� ������ ��찡 �߻��� �� �ִµ�, �̸� �����ϴ� ����� �����غ� �����Դϴ�
		BWAPI::TilePosition		getBuildLocationNear(BWAPI::UnitType buildingType, BWAPI::TilePosition desiredPosition, int buildingGapSpace, size_t constructionPlaceSearchMethod) const;

	public:

		/// static singleton ��ü�� �����մϴ�
		static ConstructionPlaceFinder & Instance();


		/// seedPosition �� seedPositionStrategy �Ķ���͸� Ȱ���ؼ� �ǹ� �Ǽ� ���� ��ġ�� Ž���ؼ� �����մϴ�
		/// seedPosition �������� ������ ���� �����ϰų�, seedPositionStrategy �� ���� ���� �м���� �ش� ���� �������� ������ ���� �����մϴ�
		/// seedPosition, seedPositionStrategy �� �Է����� ������, MainBaseLocation �������� ������ ���� �����մϴ�
		BWAPI::TilePosition		getBuildLocationWithSeedPositionAndStrategy(BWAPI::UnitType buildingType, BWAPI::TilePosition seedPosition = BWAPI::TilePositions::None, BuildOrderItem::SeedPositionStrategy seedPositionStrategy = BuildOrderItem::SeedPositionStrategy::MainBaseLocation) const;

		/// desiredPosition ��ó���� �ǹ� �Ǽ� ���� ��ġ�� Ž���ؼ� �����մϴ�
		/// desiredPosition �������� ������ ���� ã�� ��ȯ�մϴ�
		/// desiredPosition �� valid �� ���� �ƴ϶��, desiredPosition �� MainBaseLocation �� �ؼ� ������ ã�´�
		/// Returns a suitable TilePosition to build a given building type near specified TilePosition aroundTile.
		/// Returns BWAPI::TilePositions::None, if suitable TilePosition is not exists (�ٸ� ���ֵ��� �ڸ��� �־, Pylon, Creep, �ǹ����� Ÿ�� ������ ���� ���� ��� ��)
		BWAPI::TilePosition		getBuildLocationNear(BWAPI::UnitType buildingType, BWAPI::TilePosition desiredPosition) const;

		/// seedPosition ��ó���� Refinery �ǹ� �Ǽ� ���� ��ġ�� Ž���ؼ� �����մϴ�
		/// �������� ���� ���� ���� (Resource_Vespene_Geyser) �� ����Ǿ����� ���� ��(isReservedTile), �ٸ� ���� �ƴ� ��, �̹� Refinery �� �������������� �� �� 
		/// seedPosition �� ���� ����� ���� �����մϴ�
		BWAPI::TilePosition		getRefineryPositionNear(BWAPI::TilePosition seedPosition = BWAPI::TilePositions::None) const;
		
		bool					isBuildableTile(const ConstructionTask & b,int x,int y) const;		///< �ǹ� �Ǽ� ���� Ÿ������ ���θ� �����մϴ�
		void					reserveTiles(BWAPI::TilePosition position, int width, int height);	///< �ǹ� �Ǽ� ���� Ÿ�Ϸ� �����ؼ�, �ٸ� �ǹ��� �ߺ��ؼ� ���� �ʵ��� �մϴ�
		void					freeTiles(BWAPI::TilePosition position, int width, int height);		///< �ǹ� �Ǽ� ���� Ÿ�Ϸ� �����ߴ� ���� �����մϴ�
		bool					isReservedTile(int x, int y) const;
		std::vector< std::vector<bool> > & getReserveMap();											///< reserveMap�� �����մϴ�

		
		std::set< BWAPI::TilePosition > & getTilesToAvoid();										///< BaseLocation �� Mineral / Geyser ������ Ÿ�ϵ��� ����� �����մϴ�		
		bool					isTilesToAvoid(int x, int y) const;									///< (x, y) �� BaseLocation �� Mineral / Geyser ������ Ÿ�Ͽ� �ش��ϴ��� ���θ� �����մϴ�

		/// �ش� ��ġ�� BaseLocation �� ��ġ���� ���θ� �����մϴ�
		/// BaseLocation ���� ResourceDepot �ǹ��� �Ǽ��ϰ�, �ٸ� �ǹ��� �Ǽ����� �ʱ� �����Դϴ�
		bool					isOverlapsWithBaseLocation(BWAPI::TilePosition tile,BWAPI::UnitType type) const;	
		
		/// �ش� ��ġ�� �ǹ� �Ǽ��� �������� ���θ� �����մϴ�
		/// Broodwar �� canBuildHere �� _reserveMap �� isOverlapsWithBaseLocation �� üũ�մϴ�
		bool					canBuildHere(BWAPI::TilePosition position, const ConstructionTask & b) const;

		/// �ش� ��ġ�� �ǹ� �Ǽ��� �������� ���θ� buildingGapSpace ������ �����ؼ� �Ǵ��Ͽ� �����մϴ�
		/// Broodwar �� canBuildHere, isBuildableTile, isReservedTile �� üũ�մϴ�
		bool					canBuildHereWithSpace(BWAPI::TilePosition position, const ConstructionTask & b, int buildingGapSpace = 2) const;

	};
}