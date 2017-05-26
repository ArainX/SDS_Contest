#pragma once

#include "Common.h"
#include "BuildOrderQueue.h"
#include "ConstructionManager.h"

namespace MyBot
{
	/// ����(�ǹ� �Ǽ� / ���� �Ʒ� / ��ũ ����ġ / ���׷��̵�) ����� ���������� �����ϱ� ���� ���� ť�� �����ϰ�, ���� ť�� �ִ� ����� �ϳ��� �����ϴ� class
	/// ���� ��� �� �ǹ� �Ǽ� ����� ConstructionManager�� �����մϴ�
	/// @see ConstructionManager
	class BuildManager
	{
		BuildManager();

		/// �ش� MetaType �� build �� �� �ִ� producer �� ã�� ��ȯ�մϴ�
		/// @param t �����Ϸ��� ����� Ÿ��
		/// @param closestTo �Ķ��Ÿ �Է� �� producer �ĺ��� �� �ش� position ���� ���� ����� producer �� �����մϴ�
		/// @param producerID �Ķ��Ÿ �Է� �� �ش� ID�� unit �� producer �ĺ��� �� �� �ֽ��ϴ�
		BWAPI::Unit			getProducer(MetaType t, BWAPI::Position closestTo = BWAPI::Positions::None, int producerID = -1);

		/// �ش� MetaType �� build �� �� �ִ�, getProducer ���ϰ��� �ٸ� producer �� ã�� ��ȯ�մϴ�
		/// �����佺 ���� ���� �� Protoss_Archon / Protoss_Dark_Archon �� ������ �� ����մϴ�
		BWAPI::Unit			getAnotherProducer(BWAPI::Unit producer, BWAPI::Position closestTo = BWAPI::Positions::None);

		BWAPI::Unit         getClosestUnitToPosition(const BWAPI::Unitset & units, BWAPI::Position closestTo);
		BWAPI::Unit         selectUnitOfType(BWAPI::UnitType type, BWAPI::Position closestTo = BWAPI::Position(0, 0));

		int                 getAvailableMinerals();
		int                 getAvailableGas();
		bool                hasEnoughResources(MetaType type);
		bool                hasNumCompletedUnitType(BWAPI::UnitType type, int num);

		bool                canMakeNow(BWAPI::Unit producer, MetaType t);
		bool                canMake(MetaType t);

		BWAPI::TilePosition getDesiredPosition(BWAPI::UnitType unitType, BWAPI::TilePosition seedPosition, BuildOrderItem::SeedPositionStrategy seedPositionStrategy);

		void				checkBuildOrderQueueDeadlockAndAndFixIt();

	public:
		/// static singleton ��ü�� �����մϴ�
		static BuildManager &	Instance();

		/// buildQueue �� ���� Dead lock �� ������ �����ϰ�, ���� �켱������ ���� BuildOrderItem �� ����ǵ��� �õ��մϴ�
		void				update();

		/// BuildOrderItem ���� ����� �����ϴ� buildQueue 
		BuildOrderQueue     buildQueue;

		/// BuildOrderItem ���� ����� �����ϴ� buildQueue �� �����մϴ�
		BuildOrderQueue *	getBuildQueue();

		/// buildQueue �� Dead lock ���θ� �Ǵ��ϱ� ����, ���� �켱������ ���� BuildOrderItem �� producer �� �����ϰԵ� ������ ���θ� �����մϴ�
		bool				isProducerWillExist(BWAPI::UnitType producerType);

	};


}