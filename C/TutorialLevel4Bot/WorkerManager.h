#pragma once

#include "Common.h"
#include "InformationManager.h"

namespace MyBot
{
	class WorkerManager
	{
		// Worker ~ Mineral Field �� assign ���踦 �����ϴ� map
		std::map<BWAPI::Unit, BWAPI::Unit> workerMineralAssignment;

		// ������ Mineral Field �� assign �� Worker ���� �� �����ϴ� map
		std::map<BWAPI::Unit, int> workerCountOnMineral;

		WorkerManager();

	public:
		static		WorkerManager &  Instance();

		void        update();

		void		updateWorkers1();
		void		updateWorkers2();
		void		updateWorkers3();

		BWAPI::Unit getClosestMineralFrom(BWAPI::Unit worker);
		BWAPI::Unit getBestMineralTo(BWAPI::Unit worker);

		void        increaseWorkerCountOnMineral(BWAPI::Unit mineral, int num);

		void        onUnitDestroy(BWAPI::Unit unit);
		void        onUnitMorph(BWAPI::Unit unit);
	};
}