#pragma once

#include "Common.h"
#include "UnitData.h"

namespace MyBot
{
	/// ���� ��Ȳ���� �� �Ϻθ� ��ü �ڷᱸ�� �� �����鿡 �����ϰ� ������Ʈ�ϴ� class
	/// ���� ���� ��Ȳ������ BWAPI::Broodwar �� ��ȸ�Ͽ� �ľ��� �� ������, ���� ���� ��Ȳ������ BWAPI::Broodwar �� ���� ��ȸ�� �Ұ����ϱ� ������ InformationManager���� ���� �����ϵ��� �մϴ�
	/// ����, BWAPI::Broodwar �� BWTA ���� ���� ��ȸ�� �� �ִ� ���������� ��ó�� / ���� �����ϴ� ���� ������ �͵� InformationManager���� ���� �����ϵ��� �մϴ�
	class InformationManager 
	{
		InformationManager();

		/// Player - UnitData(�� Unit �� �� Unit�� UnitInfo �� Map ���·� �����ϴ� �ڷᱸ��) �� �����ϴ� �ڷᱸ�� ��ü
		std::map<BWAPI::Player, UnitData>							_unitData;

		std::map<BWAPI::Player, BWTA::BaseLocation * >				_mainBaseLocations;
		std::map<BWAPI::Player, std::list<BWTA::BaseLocation *> >	_occupiedBaseLocations;
		std::map<BWAPI::Player, std::set<BWTA::Region *> >			_occupiedRegions;

		std::map<BWAPI::Player, BWTA::Chokepoint *>					_firstChokePoint;
		std::map<BWAPI::Player, BWTA::BaseLocation *>				_firstExpansionLocation;
		std::map<BWAPI::Player, BWTA::Chokepoint *>					_secondChokePoint;
	
		/// ��ü unit �� ������ ������Ʈ �մϴ� (UnitType, lastPosition, HitPoint ��)
		void                    updateUnitsInfo();

		/// �ش� unit �� ������ ������Ʈ �մϴ� (UnitType, lastPosition, HitPoint ��)
		void                    updateUnitInfo(BWAPI::Unit unit);
		void                    updateBaseLocationInfo();
		void					updateChokePointAndExpansionLocation();
		void                    updateOccupiedRegions(BWTA::Region * region, BWAPI::Player player);

	public:

		/// static singleton ��ü�� �����մϴ�
		static InformationManager & Instance();
			
		BWAPI::Player       selfPlayer;		///< �Ʊ� Player		
		BWAPI::Race			selfRace;		///< �Ʊ� Player�� ����		
		BWAPI::Player       enemyPlayer;	///< ���� Player		
		BWAPI::Race			enemyRace;		///< ���� Player�� ����  
		
		/// Unit �� BaseLocation, ChokePoint � ���� ������ ������Ʈ�մϴ�
		void                    update();

		/// Unit �� ���� ������ ������Ʈ�մϴ�
		void					onUnitShow(BWAPI::Unit unit)        { updateUnitInfo(unit); }
		/// Unit �� ���� ������ ������Ʈ�մϴ�
		void					onUnitHide(BWAPI::Unit unit)        { updateUnitInfo(unit); }
		/// Unit �� ���� ������ ������Ʈ�մϴ�
		void					onUnitCreate(BWAPI::Unit unit)		{ updateUnitInfo(unit); }
		/// Unit �� ���� ������ ������Ʈ�մϴ�
		void					onUnitComplete(BWAPI::Unit unit)    { updateUnitInfo(unit); }
		/// Unit �� ���� ������ ������Ʈ�մϴ�
		void					onUnitMorph(BWAPI::Unit unit)       { updateUnitInfo(unit); }
		/// Unit �� ���� ������ ������Ʈ�մϴ�
		void					onUnitRenegade(BWAPI::Unit unit)    { updateUnitInfo(unit); }
		/// Unit �� ���� ������ ������Ʈ�մϴ� 
		/// ������ �ı�/����� ���, �ش� ���� ������ �����մϴ�
		void					onUnitDestroy(BWAPI::Unit unit);
			
		
		/// �ش� BaseLocation �� player�� �ǹ��� �����ϴ��� �����մϴ�
		/// @param baseLocation ��� BaseLocation
		/// @param player �Ʊ� / ����
		/// @param radius TilePosition ����
		bool					hasBuildingAroundBaseLocation(BWTA::BaseLocation * baseLocation, BWAPI::Player player, int radius = 10);
		
		/// �ش� Region �� ���� �ǹ��� �����ϴ��� �����մϴ�
		bool					existsEnemyBuildingInRegion(BWTA::Region * region);
		
		/// �ش� Region �� �Ʊ� �ǹ��� �����ϴ��� �����մϴ�
		bool					existsMyBuildingInRegion(BWTA::Region * region);
		

		/// �ش� Player (�Ʊ� or ����) �� �ǹ��� �Ǽ��ؼ� ������ Region ����� �����մϴ�
		std::set<BWTA::Region *> &  getOccupiedRegions(BWAPI::Player player);

		/// �ش� Player (�Ʊ� or ����) �� �ǹ��� �Ǽ��ؼ� ������ BaseLocation ����� �����մϴ�		 
		std::list<BWTA::BaseLocation *> & getOccupiedBaseLocations(BWAPI::Player player);

		/// �ش� Player (�Ʊ� or ����) �� Main BaseLocation �� �����մϴ�		 
		BWTA::BaseLocation *	getMainBaseLocation(BWAPI::Player player);

		/// �ش� Player (�Ʊ� or ����) �� Main BaseLocation ���� ���� ����� ChokePoint �� �����մϴ�		 
		BWTA::Chokepoint *      getFirstChokePoint(BWAPI::Player player);

		/// �ش� Player (�Ʊ� or ����) �� Main BaseLocation ���� ���� ����� Expansion BaseLocation �� �����մϴ�		 
		BWTA::BaseLocation *    getFirstExpansionLocation(BWAPI::Player player);

		/// �ش� Player (�Ʊ� or ����) �� Main BaseLocation ���� �ι�°�� ����� ChokePoint �� �����մϴ�		 
		BWTA::Chokepoint *      getSecondChokePoint(BWAPI::Player player);



		/// �ش� Player (�Ʊ� or ����) �� ��� ���� ��� (���� �ֱٰ�) UnitAndUnitInfoMap �� �����մϴ�		 
		/// �ľǵ� �������� �����ϱ� ������ ������ ������ Ʋ�� ���� �� �ֽ��ϴ�
		const UnitAndUnitInfoMap &           getUnitInfo(BWAPI::Player player) const;
		/// �ش� Player (�Ʊ� or ����) �� ��� ���� ��� UnitData �� �����մϴ�		 
		const UnitData &        getUnitData(BWAPI::Player player) const;


		/// �ش� Player (�Ʊ� or ����) �� �ش� UnitType ���� ���ڸ� �����մϴ� (�Ʒ�/�Ǽ� ���� ���� ���ڱ��� ����)
		int						getNumUnits(BWAPI::UnitType type, BWAPI::Player player);

		/// �ش� Player (�Ʊ� or ����) �� position ������ ���� ����� unitInfo �� �����մϴ�		 
		void                    getNearbyForce(std::vector<UnitInfo> & unitInfo, BWAPI::Position p, BWAPI::Player player, int radius);

		/// �ش� UnitType �� ���� �������� �����մϴ�
		bool					isCombatUnit(BWAPI::UnitType type) const;



		// �ش� ������ UnitType �� ResourceDepot ����� �ϴ� UnitType�� �����մϴ�
		BWAPI::UnitType			getBasicResourceDepotBuildingType(BWAPI::Race race = BWAPI::Races::None);

		// �ش� ������ UnitType �� Refinery ����� �ϴ� UnitType�� �����մϴ�
		BWAPI::UnitType			getRefineryBuildingType(BWAPI::Race race = BWAPI::Races::None);

		// �ش� ������ UnitType �� SupplyProvider ����� �ϴ� UnitType�� �����մϴ�
		BWAPI::UnitType			getBasicSupplyProviderUnitType(BWAPI::Race race = BWAPI::Races::None);

		// �ش� ������ UnitType �� Worker �� �ش��ϴ� UnitType�� �����մϴ�
		BWAPI::UnitType			getWorkerType(BWAPI::Race race = BWAPI::Races::None);

		// �ش� ������ UnitType �� Basic Combat Unit �� �ش��ϴ� UnitType�� �����մϴ�
		BWAPI::UnitType			getBasicCombatUnitType(BWAPI::Race race = BWAPI::Races::None);

		// �ش� ������ UnitType �� Basic Combat Unit �� �����ϱ� ���� �Ǽ��ؾ��ϴ� UnitType�� �����մϴ�
		BWAPI::UnitType			getBasicCombatBuildingType(BWAPI::Race race = BWAPI::Races::None);

		// �ش� ������ UnitType �� Advanced Combat Unit �� �ش��ϴ� UnitType�� �����մϴ�
		BWAPI::UnitType			getAdvancedCombatUnitType(BWAPI::Race race = BWAPI::Races::None);

		// �ش� ������ UnitType �� Observer �� �ش��ϴ� UnitType�� �����մϴ�
		BWAPI::UnitType			getObserverUnitType(BWAPI::Race race = BWAPI::Races::None);

		// �ش� ������ UnitType �� Basic Depense ����� �ϴ� UnitType�� �����մϴ�
		BWAPI::UnitType			getBasicDefenseBuildingType(BWAPI::Race race = BWAPI::Races::None);

		// �ش� ������ UnitType �� Advanced Depense ����� �ϴ� UnitType�� �����մϴ�
		BWAPI::UnitType			getAdvancedDefenseBuildingType(BWAPI::Race race = BWAPI::Races::None);
	};
}
