#pragma once

#include "Common.h"
#include "MapTools.h"
#include "WorkerManager.h"
#include "ScoutManager.h"
#include "ConstructionPlaceFinder.h"
#include "InformationManager.h"
#include "StrategyManager.h"

namespace MyBot
{
	/// �ǹ� �Ǽ� Construction ��� ����� ����Ʈ�� �����ϰ�, �ǹ� �Ǽ� ����� �� ����ǵ��� ��Ʈ���ϴ� class
	class ConstructionManager
	{
		ConstructionManager();

		/// �Ǽ� �ʿ� �ڿ��� �̸� �����س���, 
		/// �Ǽ� ��� ��Ұ� �̰�ô ����� ��� �Ǽ� �ϲ��� �̵����� �ᱹ �Ǽ��� ���۵ǰ� �ϰ�, 
		/// �Ǽ� �ϲ��� ���߿� �״� ��� �ٸ� �Ǽ� �ϲ��� �����Ͽ� �Ǽ��� �����ϰ� �ϱ� ����
		/// Construction Task ���� ����� constructionQueue �� �����մϴ�
		std::vector<ConstructionTask> constructionQueue;

		int             reservedMinerals;				///< minerals reserved for planned buildings
		int             reservedGas;					///< gas reserved for planned buildings

		bool            isEvolvedBuilding(BWAPI::UnitType type);
		bool            isBuildingPositionExplored(const ConstructionTask & b) const;

		/// constructionQueue ���� �Ǽ� ���°� UnderConstruction �� ConstructionTask �������� �����մϴ�
		/// �Ǽ��� �����߾��� ConstructionTask �̱� ������ _reservedMinerals, _reservedGas �� �ǵ帮�� �ʴ´�
		void            removeCompletedConstructionTasks(const std::vector<ConstructionTask> & toRemove);

		/// STEP 1: DO BOOK KEEPING ON WORKERS WHICH MAY HAVE DIED
		void            validateWorkersAndBuildings();
		/// STEP 2: ASSIGN WORKERS TO BUILDINGS WITHOUT THEM
		void            assignWorkersToUnassignedBuildings();
		/// STEP 3: ISSUE CONSTRUCTION ORDERS TO ASSIGN BUILDINGS AS NEEDED
		void            constructAssignedBuildings();
		/// STEP 4: UPDATE DATA STRUCTURES FOR BUILDINGS STARTING CONSTRUCTION
		void            checkForStartedConstruction();
		/// STEP 5: IF WE ARE TERRAN, THIS MATTERS
		/// �׶��� �Ǽ��� ������ ��, �Ǽ� ���߿� �ϲ��� ���� �� �ֽ��ϴ�. �� ���, �ǹ��� ���� �ٽ� �ٸ� SCV�� �Ҵ��մϴ�
		/// �����, �����佺 / ���״� �Ǽ��� �����ϸ� �ϲ� �����͸� null �� ����� ������ (constructionWorker = null) �Ǽ� ���߿� ���� �ϲ��� �Ű澵 �ʿ� �����ϴ� 
		void            checkForDeadTerranBuilders();
		/// STEP 6: CHECK FOR COMPLETED BUILDINGS
		void            checkForCompletedBuildings();
		
		/// �Ǽ� ������� üũ�ϰ�, �ذ��մϴ�
		void            checkForDeadlockConstruction();	

	public:
		/// static singleton ��ü�� �����մϴ�
		static ConstructionManager &	Instance();

		/// constructionQueue �� ���� Dead lock �� ������ �����ϰ�, constructionQueue �� ConstructionTask ���� ����ǵ��� �����մϴ�
		void                update();

		/// constructionQueue �� �����մϴ�
		std::vector<ConstructionTask> * getConstructionQueue();

		/// constructionQueue �� ConstructionTask �� �߰��մϴ�
		void                addConstructionTask(BWAPI::UnitType type,BWAPI::TilePosition desiredPosition);
		/// constructionQueue ���� ConstructionTask �� ����մϴ�
		void				cancelConstructionTask(BWAPI::UnitType type, BWAPI::TilePosition desiredPosition);
		
		/// constructionQueue �� ConstructionTask ������ �����մϴ�
		/// queryTilePosition �� �Է��� ���, ��ġ�� �Ÿ������� ����մϴ�
		int                 getConstructionQueueItemCount(BWAPI::UnitType queryType, BWAPI::TilePosition queryTilePosition = BWAPI::TilePositions::None);

		/// constructionQueue �� ConstructionTask ������ �����մϴ�
		std::vector<BWAPI::UnitType> buildingsQueued();

		/// Construction �� ���� �����ص� Mineral ���ڸ� �����մϴ�
		int                 getReservedMinerals();
		/// Construction �� ���� �����ص� Gas ���ڸ� �����մϴ�
		int                 getReservedGas();
	};
}