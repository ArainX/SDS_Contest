#pragma once

#include "Common.h"
#include "WorkerData.h"
#include "ConstructionTask.h"
#include "ConstructionManager.h"
#include "InformationManager.h"

namespace MyBot
{
	/// �ϲ� ���ֵ��� ���¸� �����ϰ� ��Ʈ���ϴ� class
	class WorkerManager
	{
		/// �� Worker �� ���� WorkerJob ��Ȳ�� �����ϴ� �ڷᱸ�� ��ü
		WorkerData  workerData;

		/// �ϲ� �� �Ѹ��� Repair Worker �� ���ؼ�, ��ü ���� ����� �ϳ��� ������� �����մϴ�
		BWAPI::Unit currentRepairWorker;
				
		void        updateWorkerStatus();

		/// Idle �ϲ��� Mineral �ϲ����� ����ϴ�
		void        handleIdleWorkers();

		void        handleGasWorkers();
		void        handleMoveWorkers();
		void        handleCombatWorkers();
		void        handleRepairWorkers();

		void        rebalanceWorkers();

		WorkerManager();

	public:
		/// static singleton ��ü�� �����մϴ�
		static WorkerManager &  Instance();

		/// �ϲ� ���ֵ��� ���¸� �����ϴ� workerData ��ü�� ������Ʈ�ϰ�, �ϲ� ���ֵ��� �ڿ� ä�� �� �ӹ� ������ �ϵ��� �մϴ�
		void        update();

		/// �ϲ� ���ֵ��� ���¸� �����ϴ� workerData ��ü�� ������Ʈ�մϴ�
		void        onUnitDestroy(BWAPI::Unit unit);

		/// �ϲ� ���ֵ��� ���¸� �����ϴ� workerData ��ü�� ������Ʈ�մϴ�
		void        onUnitMorph(BWAPI::Unit unit);

		/// �ϲ� ���ֵ��� ���¸� �����ϴ� workerData ��ü�� ������Ʈ�մϴ�
		void        onUnitShow(BWAPI::Unit unit);
		
		
		/// �ϲ� ���ֵ��� ���¸� �����ϴ� workerData ��ü�� �����մϴ�
		WorkerData  getWorkerData();



		/// �ش� �ϲ� ���� unit �� WorkerJob ���� Idle �� �����մϴ�
		void        setIdleWorker(BWAPI::Unit unit);
		
		/// idle ������ �ϲ� ���� unit �� ���ڸ� �����մϴ�
		int         getNumIdleWorkers();



		/// �ش� �ϲ� ���� unit �� WorkerJob ���� Mineral �� �����մϴ�
		void        setMineralWorker(BWAPI::Unit unit);
		int         getNumMineralWorkers();
		bool        isMineralWorker(BWAPI::Unit worker);

		/// target ���κ��� ���� ����� Mineral �ϲ� ������ �����մϴ�
		BWAPI::Unit getClosestMineralWorkerTo(BWAPI::Position target);

		/// �ش� �ϲ� ���� unit ���κ��� ���� ����� ResourceDepot �ǹ��� �����մϴ�
		BWAPI::Unit getClosestResourceDepotFromWorker(BWAPI::Unit worker);


		/// Mineral �ϲ� ���ֵ� �߿��� Gas �ӹ��� ������ �ϲ� ������ ���ؼ� �����մϴ�
		/// Idle �ϲ��� Build, Repair, Scout �� �ٸ� �ӹ��� ���� ���ԵǾ�� �ϱ� ������ Mineral �ϲ� �߿����� ���մϴ�
		BWAPI::Unit chooseGasWorkerFromMineralWorkers(BWAPI::Unit refinery);
		int         getNumGasWorkers();

		/// Mineral Ȥ�� Idle �ϲ� ���ֵ� �߿��� Scout �ӹ��� ������ �ϲ� ������ ���ؼ� �����մϴ�
		BWAPI::Unit getScoutWorker();
		void        setScoutWorker(BWAPI::Unit worker);
		bool        isScoutWorker(BWAPI::Unit worker);

		/// buildingPosition ���� ���� ����� Move Ȥ�� Idle Ȥ�� Mineral �ϲ� ���ֵ� �߿��� Construction �ӹ��� ������ �ϲ� ������ ���ؼ� �����մϴ�
		/// Move / Idle Worker �߿��� ���� �����ϰ�, ������ Mineral Worker �߿��� �����մϴ�
		/// �ϲ� ������ 2�� �̻��̸�, avoidWorkerID �� �ش��ϴ� worker �� �������� �ʵ��� �մϴ�
		/// if setJobAsConstructionWorker is true (default), it will be flagged as a builder unit
		/// if setJobAsConstructionWorker is false, we just want to see which worker will build a building
		BWAPI::Unit chooseConstuctionWorkerClosestTo(BWAPI::UnitType buildingType, BWAPI::TilePosition buildingPosition, bool setJobAsConstructionWorker = true, int avoidWorkerID = 0);
		void        setConstructionWorker(BWAPI::Unit worker, BWAPI::UnitType buildingType);
		bool        isConstructionWorker(BWAPI::Unit worker);

		/// position ���� ���� ����� Mineral Ȥ�� Idle Ȥ�� Move �ϲ� ���ֵ� �߿��� Repair �ӹ��� ������ �ϲ� ������ ���ؼ� �����մϴ�
		BWAPI::Unit chooseRepairWorkerClosestTo(BWAPI::Position p, int maxRange = 100000000);
		void        setRepairWorker(BWAPI::Unit worker, BWAPI::Unit unitToRepair);
		void        stopRepairing(BWAPI::Unit worker);
	
		/// position ���� ���� ����� Mineral Ȥ�� Idle �ϲ� ���ֵ� �߿��� Move �ӹ��� ������ �ϲ� ������ ���ؼ� �����մϴ�
		void        setMoveWorker(BWAPI::Unit worker, int m, int g, BWAPI::Position p);
		BWAPI::Unit chooseMoveWorkerClosestTo(BWAPI::Position p);

		/// �ش� �ϲ� �������κ��� ���� ����� ���� ������ �����մϴ�
		BWAPI::Unit getClosestEnemyUnitFromWorker(BWAPI::Unit worker);

		/// �ش� �ϲ� ���ֿ��� Combat �ӹ��� �ο��մϴ�
		void        setCombatWorker(BWAPI::Unit worker);
		/// ��� Combat �ϲ� ���ֿ� ���� �ӹ��� �����մϴ�
		void        stopCombat();

	};
}