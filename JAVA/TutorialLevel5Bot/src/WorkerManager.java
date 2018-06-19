import bwapi.Color;
import bwapi.Position;
import bwapi.Race;
import bwapi.TilePosition;
import bwapi.Unit;
import bwapi.UnitType;
import bwta.BWTA;

/// �ϲ� ���ֵ��� ���¸� �����ϰ� ��Ʈ���ϴ� class
public class WorkerManager {

	/// �� Worker �� ���� WorkerJob ��Ȳ�� �����ϴ� �ڷᱸ�� ��ü
	private WorkerData workerData = new WorkerData();
	
	private CommandUtil commandUtil = new CommandUtil();
	
	/// �ϲ� �� �Ѹ��� Repair Worker �� ���ؼ�, ��ü ���� ����� �ϳ��� ������� �����մϴ�
	private Unit currentRepairWorker = null;
	
	private static WorkerManager instance = new WorkerManager();
	
	/// static singleton ��ü�� �����մϴ�
	public static WorkerManager Instance() {
		return instance;
	}
	
	/// �ϲ� ���ֵ��� ���¸� �����ϴ� workerData ��ü�� ������Ʈ�ϰ�, �ϲ� ���ֵ��� �ڿ� ä�� �� �ӹ� ������ �ϵ��� �մϴ�
	public void update() {

		// 1�ʿ� 1���� �����Ѵ�
		if (MyBotModule.Broodwar.getFrameCount() % 24 != 0) return;

		updateWorkerStatus();
		handleGasWorkers();
		handleIdleWorkers();
		handleMoveWorkers();
		handleCombatWorkers();
		handleRepairWorkers();
	}
	
	public void updateWorkerStatus() 
	{
		// Drone �� �Ǽ��� ���� isConstructing = true ���·� �Ǽ���ұ��� �̵��� ��, 
		// ��� getBuildType() == none �� �Ǿ��ٰ�, isConstructing = true, isMorphing = true �� �� ��, �Ǽ��� �����Ѵ�

		// for each of our Workers
		for (Unit worker : workerData.getWorkers())
		{
			//if (workerData.getWorkerJob(worker) == WorkerData::Build && worker.getBuildType() == BWAPI::UnitTypes::None)
			//{
			//	std::cout << "construction worker " << worker.getID() << "buildtype BWAPI::UnitTypes::None " << std::endl;
			//}

			/*
			if (worker.isCarryingMinerals()) {
				std::cout << "mineral worker isCarryingMinerals " << worker.getID() 
					<< " isIdle: " << worker.isIdle()
					<< " isCompleted: " << worker.isCompleted()
					<< " isInterruptible: " << worker.isInterruptible()
					<< " target Name: " << worker.getTarget().getType().getName()
					<< " job: " << workerData.getWorkerJob(worker)
					<< " exists " << worker.exists()
					<< " isConstructing " << worker.isConstructing()
					<< " isMorphing " << worker.isMorphing()
					<< " isMoving " << worker.isMoving()
					<< " isBeingConstructed " << worker.isBeingConstructed()
					<< " isStuck " << worker.isStuck()
					<< std::endl;
			}
			*/

			if (!worker.isCompleted())
			{
				continue;
			}

			// ���ӻ󿡼� worker�� isIdle ���°� �Ǿ����� (���� ź���߰ų�, ���� �ӹ��� ���� ���), WorkerData �� Idle �� ���� ��, handleGasWorkers, handleIdleWorkers ��� �� �ӹ��� �����Ѵ� 
			if ( worker.isIdle() )
			{
				/*
				if ((workerData.getWorkerJob(worker) == WorkerData::Build)
					|| (workerData.getWorkerJob(worker) == WorkerData::Move)
					|| (workerData.getWorkerJob(worker) == WorkerData::Scout)) {

					std::cout << "idle worker " << worker.getID()
						<< " job: " << workerData.getWorkerJob(worker)
						<< " exists " << worker.exists()
						<< " isConstructing " << worker.isConstructing()
						<< " isMorphing " << worker.isMorphing()
						<< " isMoving " << worker.isMoving()
						<< " isBeingConstructed " << worker.isBeingConstructed()
						<< " isStuck " << worker.isStuck()
						<< std::endl;
				}
				*/

				// workerData ���� Build / Move / Scout �� �ӹ������� ���, worker �� �� �ӹ� ���� ���� (�ӹ� �Ϸ� ��) �� �Ͻ������� isIdle ���°� �� �� �ִ� 
				if ((workerData.getWorkerJob(worker) != WorkerData.WorkerJob.Build)
					&& (workerData.getWorkerJob(worker) != WorkerData.WorkerJob.Move)
					&& (workerData.getWorkerJob(worker) != WorkerData.WorkerJob.Scout))  
				{
					workerData.setWorkerJob(worker, WorkerData.WorkerJob.Idle, (Unit)null);
				}
			}

			// if its job is gas
			if (workerData.getWorkerJob(worker) == WorkerData.WorkerJob.Gas)
			{
				Unit refinery = workerData.getWorkerResource(worker);

				// if the refinery doesn't exist anymore (�ı��Ǿ��� ���)
				if (refinery == null || !refinery.exists() ||	refinery.getHitPoints() <= 0)
				{
					workerData.setWorkerJob(worker, WorkerData.WorkerJob.Idle, (Unit)null);
				}
			}

			// if its job is repair
			if (workerData.getWorkerJob(worker) == WorkerData.WorkerJob.Repair)
			{
				Unit repairTargetUnit = workerData.getWorkerRepairUnit(worker);
							
				// ����� �ı��Ǿ��ų�, ������ �� ���� ���
				if (repairTargetUnit == null || !repairTargetUnit.exists() || repairTargetUnit.getHitPoints() <= 0 || repairTargetUnit.getHitPoints() == repairTargetUnit.getType().maxHitPoints())
				{
					workerData.setWorkerJob(worker, WorkerData.WorkerJob.Idle, (Unit)null);
				}
			}
		}
	}


	public void handleGasWorkers()
	{
		// for each unit we have
		for (Unit unit : MyBotModule.Broodwar.self().getUnits())
		{
			// refinery �� �Ǽ� completed �Ǿ�����,
			if (unit.getType().isRefinery() && unit.isCompleted() )
			{
				// get the number of workers currently assigned to it
				int numAssigned = workerData.getNumAssignedWorkers(unit);

				// if it's less than we want it to be, fill 'er up
				// ���� : �̳׶� �ϲ��� ������ ���� �ϲ��� ������ 3~4���� ��� �߻�.
				for (int i = 0; i<(Config.WorkersPerRefinery - numAssigned); ++i)
				{
					Unit gasWorker = chooseGasWorkerFromMineralWorkers(unit);
					if (gasWorker != null)
					{
						workerData.setWorkerJob(gasWorker, WorkerData.WorkerJob.Gas, unit);
					}
				}
			}
		}
	}

	/// Idle �ϲ��� Mineral �ϲ����� ����ϴ�
	public void handleIdleWorkers() 
	{
		// for each of our workers
		for (Unit worker : workerData.getWorkers())
		{
			if (worker == null) continue;

			// if worker's job is idle 
			if (workerData.getWorkerJob(worker) == WorkerData.WorkerJob.Idle || workerData.getWorkerJob(worker) == WorkerData.WorkerJob.Default )
			{
				// send it to the nearest mineral patch
				setMineralWorker(worker);
			}
		}
	}

	public void handleMoveWorkers()
	{
		// for each of our workers
		for (Unit worker : workerData.getWorkers())
		{
			if (worker == null) continue;

			// if it is a move worker
			if (workerData.getWorkerJob(worker) == WorkerData.WorkerJob.Move)
			{
				WorkerMoveData data = workerData.getWorkerMoveData(worker);

				// �������� ������ ��� �̵� ����� �����Ѵ�
				if (worker.getPosition().getDistance(data.getPosition()) < 4) {
					setIdleWorker(worker);
				}
				else {
					commandUtil.move(worker, data.getPosition());
				}
			}
		}
	}

	// bad micro for combat workers
	public void handleCombatWorkers()
	{
		for (Unit worker : workerData.getWorkers())
		{
			if (worker == null) continue;

			if (workerData.getWorkerJob(worker) == WorkerData.WorkerJob.Combat)
			{
				MyBotModule.Broodwar.drawCircleMap(worker.getPosition().getX(), worker.getPosition().getY(), 4, Color.Yellow, true);
				Unit target = getClosestEnemyUnitFromWorker(worker);

				if (target != null)
				{
					commandUtil.attackUnit(worker, target);
				}
			}
		}
	}


	public void handleRepairWorkers()
	{
		if (MyBotModule.Broodwar.self().getRace() != Race.Terran)
		{
			return;
		}

		for (Unit unit : MyBotModule.Broodwar.self().getUnits())
		{
			// �ǹ��� ��� �ƹ��� �־ ������ ����. �ϲ� �Ѹ��� ������� ����
			if (unit.getType().isBuilding() && unit.isCompleted() == true && unit.getHitPoints() < unit.getType().maxHitPoints())
			{
				Unit repairWorker = chooseRepairWorkerClosestTo(unit.getPosition(), 0);
				setRepairWorker(repairWorker, unit);
				break;
			}
			// ��ī�� ���� (SCV, ������ũ, ���̾� ��)�� ��� ��ó�� SCV�� �ִ� ��� ����. �ϲ� �Ѹ��� ������� ����
			else if (unit.getType().isMechanical() && unit.isCompleted() == true && unit.getHitPoints() < unit.getType().maxHitPoints())
			{
				// SCV �� ���� ��󿡼� ����. ���� ���ָ� �����ϵ��� �Ѵ�
				if (unit.getType() != UnitType.Terran_SCV) {
					Unit repairWorker = chooseRepairWorkerClosestTo(unit.getPosition(), 10 * Config.TILE_SIZE);
					setRepairWorker(repairWorker, unit);
					break;
				}
			}

		}
	}

	/// position ���� ���� ����� Mineral Ȥ�� Idle Ȥ�� Move �ϲ� ���ֵ� �߿��� Repair �ӹ��� ������ �ϲ� ������ ���ؼ� �����մϴ�
	public Unit chooseRepairWorkerClosestTo(Position p, int maxRange)
	{
		if (!p.isValid()) return null;

	    Unit closestWorker = null;
	    double closestDist = 100000000;

		if (currentRepairWorker != null && currentRepairWorker.exists() && currentRepairWorker.getHitPoints() > 0)
	    {
			return currentRepairWorker;
	    }

	    // for each of our workers
		for (Unit worker : workerData.getWorkers())
		{
			if (worker == null)
			{
				continue;
			}

			if (worker.isCompleted() 
				&& (workerData.getWorkerJob(worker) == WorkerData.WorkerJob.Minerals || workerData.getWorkerJob(worker) == WorkerData.WorkerJob.Idle || workerData.getWorkerJob(worker) == WorkerData.WorkerJob.Move))
			{
				double dist = worker.getDistance(p);

				if (closestWorker == null || dist < closestDist)
	            {
					closestWorker = worker;
	                dist = closestDist;
	            }
			}
		}

		if (currentRepairWorker == null || currentRepairWorker.exists() == false || currentRepairWorker.getHitPoints() <= 0) {
			currentRepairWorker = closestWorker;
		}

		return closestWorker;
	}

	/// �ش� �ϲ� ���� unit �� WorkerJob ���� Mineral �� �����մϴ�
	public void setMineralWorker(Unit unit)
	{
		if (unit == null) return;

		// check if there is a mineral available to send the worker to
		Unit depot = getClosestResourceDepotFromWorker(unit);

		// if there is a valid ResourceDepot (Command Center, Nexus, Hatchery)
		if (depot != null)
		{
			// update workerData with the new job
			workerData.setWorkerJob(unit, WorkerData.WorkerJob.Minerals, depot);
		}
	}
	
	/// target ���κ��� ���� ����� Mineral �ϲ� ������ �����մϴ�
	public Unit getClosestMineralWorkerTo(Position target)
	{
		Unit closestUnit = null;
		double closestDist = 100000;

		for (Unit unit : MyBotModule.Broodwar.self().getUnits())
		{
			if (unit.isCompleted()
				&& unit.getHitPoints() > 0
				&& unit.exists()
				&& unit.getType().isWorker()
				&& WorkerManager.Instance().isMineralWorker(unit))
			{
				double dist = unit.getDistance(target);
				if (closestUnit == null || dist < closestDist)
				{
					closestUnit = unit;
					closestDist = dist;
				}
			}
		}

		return closestUnit;
	}

	/// �ش� �ϲ� ���� unit ���κ��� ���� ����� ResourceDepot �ǹ��� �����մϴ�
	public Unit getClosestResourceDepotFromWorker(Unit worker)
	{
		if (worker == null) return null;

		Unit closestDepot = null;
		double closestDistance = 0;

		for (Unit unit : MyBotModule.Broodwar.self().getUnits())
		{
			if (unit == null) continue;
			
			// ���� �����, �ϲۼ��� �� ��������, �ϼ��� ResourceDepot (Ȥ�� Lair �� Hive�� �������� �ǹ�)�� ã�´�
			if (unit.getType().isResourceDepot()
				&& (unit.isCompleted() || unit.getType() == UnitType.Zerg_Lair || unit.getType() == UnitType.Zerg_Hive) )
			{
				double distance = unit.getDistance(worker);

				// �ϴ� ���� ResourceDepot �� �ϳ��� ���õǵ��� �Ѵ�
				if (closestDepot == null)
				{
					closestDepot = unit;
					closestDistance = distance;
				}
				// �� ����� ResourceDepot �� �ְ�, �ϲ� ���� �� ���� �ʾҴٸ� �ű� ������ �Ѵ�
				else if (distance < closestDistance
					&& workerData.depotHasEnoughMineralWorkers(unit) == false) 
				{
					closestDepot = unit;
					closestDistance = distance;
				}
			}
		}

		return closestDepot;
	}

	/// �ش� �ϲ� ���� unit �� WorkerJob ���� Idle �� �����մϴ�
	public void setIdleWorker(Unit unit)
	{
		if (unit == null) return;

		workerData.setWorkerJob(unit, WorkerData.WorkerJob.Idle, (Unit)null);
	}

	/// Mineral �ϲ� ���ֵ� �߿��� Gas �ӹ��� ������ �ϲ� ������ ���ؼ� �����մϴ�
	/// Idle �ϲ��� Build, Repair, Scout �� �ٸ� �ӹ��� ���� ���ԵǾ�� �ϱ� ������ Mineral �ϲ� �߿����� ���մϴ�
	public Unit chooseGasWorkerFromMineralWorkers(Unit refinery)
	{
		if (refinery == null) return null;

		Unit closestWorker = null;
		double closestDistance = 0;

		for (Unit unit : workerData.getWorkers())
		{
			if (unit == null) continue;
			
			if (unit.isCompleted() && workerData.getWorkerJob(unit) == WorkerData.WorkerJob.Minerals)
			{
				double distance = unit.getDistance(refinery);
				if (closestWorker == null || distance < closestDistance)
				{
					closestWorker = unit;
					closestDistance = distance;
				}
			}
		}

		return closestWorker;
	}

	public void setConstructionWorker(Unit worker, UnitType buildingType)
	{
		if (worker == null) return;

		workerData.setWorkerJob(worker, WorkerData.WorkerJob.Build, buildingType);
	}

	/// buildingPosition ���� ���� ����� Move Ȥ�� Idle Ȥ�� Mineral �ϲ� ���ֵ� �߿��� Construction �ӹ��� ������ �ϲ� ������ ���ؼ� �����մϴ�
	/// Move / Idle Worker �߿��� ���� �����ϰ�, ������ Mineral Worker �߿��� �����մϴ�
	/// �ϲ� ������ 2�� �̻��̸�, avoidWorkerID �� �ش��ϴ� worker �� �������� �ʵ��� �մϴ�
	/// if setJobAsConstructionWorker is true (default), it will be flagged as a builder unit
	/// if setJobAsConstructionWorker is false, we just want to see which worker will build a building
	public Unit chooseConstuctionWorkerClosestTo(UnitType buildingType, TilePosition buildingPosition, boolean setJobAsConstructionWorker, int avoidWorkerID)
	{
		// variables to hold the closest worker of each type to the building
		Unit closestMovingWorker = null;
		Unit closestMiningWorker = null;
		double closestMovingWorkerDistance = 0;
		double closestMiningWorkerDistance = 0;

		// look through each worker that had moved there first
		for (Unit unit : workerData.getWorkers())
		{
			if (unit == null) continue;

			// worker �� 2�� �̻��̸�, avoidWorkerID �� ���Ѵ�
			if (workerData.getWorkers().size() >= 2 && avoidWorkerID != 0 && unit.getID() == avoidWorkerID) continue;

			// Move / Idle Worker
			if (unit.isCompleted() && (workerData.getWorkerJob(unit) == WorkerData.WorkerJob.Move || workerData.getWorkerJob(unit) == WorkerData.WorkerJob.Idle))
			{
				// if it is a new closest distance, set the pointer
				double distance = unit.getDistance(buildingPosition.toPosition());
				if (closestMovingWorker == null || distance < closestMovingWorkerDistance)
				{
					if (BWTA.isConnected(unit.getTilePosition(), buildingPosition)) {
						closestMovingWorker = unit;
						closestMovingWorkerDistance = distance;
					}
				}
			}

			// Move / Idle Worker �� ������, �ٸ� Worker �߿��� �����Ѵ� 
			if (unit.isCompleted() 
				&& (workerData.getWorkerJob(unit) != WorkerData.WorkerJob.Move && workerData.getWorkerJob(unit) != WorkerData.WorkerJob.Idle && workerData.getWorkerJob(unit) != WorkerData.WorkerJob.Build))
			{
				// if it is a new closest distance, set the pointer
				double distance = unit.getDistance(buildingPosition.toPosition());
				if (closestMiningWorker == null || distance < closestMiningWorkerDistance)
				{
					if (BWTA.isConnected(unit.getTilePosition(), buildingPosition)) {
						closestMiningWorker = unit;
						closestMiningWorkerDistance = distance;
					}
				}
			}
		}
		
		/*
		if (closestMiningWorker)
			std::cout << "closestMiningWorker " << closestMiningWorker.getID() << std::endl;
		if (closestMovingWorker)
			std::cout << "closestMovingWorker " << closestMovingWorker.getID() << std::endl;
		*/
		
		Unit chosenWorker = closestMovingWorker != null ? closestMovingWorker : closestMiningWorker;

		// if the worker exists (one may not have been found in rare cases)
		if (chosenWorker != null && setJobAsConstructionWorker)
		{
			workerData.setWorkerJob(chosenWorker, WorkerData.WorkerJob.Build, buildingType);
		}

		return chosenWorker;
	}
	

	/// Mineral Ȥ�� Idle �ϲ� ���ֵ� �߿��� Scout �ӹ��� ������ �ϲ� ������ ���ؼ� �����մϴ�
	public Unit getScoutWorker()
	{
	    // for each of our workers
		for (Unit worker : workerData.getWorkers())
		{
			if (worker == null)
			{
				continue;
			}
			// if it is a scout worker
	        if (workerData.getWorkerJob(worker) == WorkerData.WorkerJob.Scout) 
			{
				return worker;
			}
		}

	    return null;
	}

	// sets a worker as a scout
	public void setScoutWorker(Unit worker)
	{
		if (worker == null) return;

		workerData.setWorkerJob(worker, WorkerData.WorkerJob.Scout, (Unit)null);
	}

	
	// get a worker which will move to a current location
	public Unit chooseMoveWorkerClosestTo(Position p)
	{
		// set up the pointer
		Unit closestWorker = null;
		double closestDistance = 0;

		// for each worker we currently have
		for (Unit unit : workerData.getWorkers())
		{
			if (unit == null) continue;

			// only consider it if it's a mineral worker
			if (unit.isCompleted() && workerData.getWorkerJob(unit) == WorkerData.WorkerJob.Minerals)
			{
				// if it is a new closest distance, set the pointer
				double distance = unit.getDistance(p);
				if (closestWorker == null || distance < closestDistance)
				{
					closestWorker = unit;
					closestDistance = distance;
				}
			}
		}

		// return the worker
		return closestWorker;
	}

	/// position ���� ���� ����� Mineral Ȥ�� Idle �ϲ� ���ֵ� �߿��� Move �ӹ��� ������ �ϲ� ������ ���ؼ� �����մϴ�
	public void setMoveWorker(Unit worker, int mineralsNeeded, int gasNeeded, Position p)
	{
		// set up the pointer
		Unit closestWorker = null;
		double closestDistance = 0;

		// for each worker we currently have
		for (Unit unit : workerData.getWorkers())
		{
			if (unit == null) continue;
			
			// only consider it if it's a mineral worker or idle worker
			if (unit.isCompleted() && (workerData.getWorkerJob(unit) == WorkerData.WorkerJob.Minerals || workerData.getWorkerJob(unit) == WorkerData.WorkerJob.Idle))
			{
				// if it is a new closest distance, set the pointer
				double distance = unit.getDistance(p);
				if (closestWorker == null || distance < closestDistance)
				{
					closestWorker = unit;
					closestDistance = distance;
				}
			}
		}

		if (closestWorker != null)
		{
			workerData.setWorkerJob(closestWorker, WorkerData.WorkerJob.Move, new WorkerMoveData(mineralsNeeded, gasNeeded, p));
		}
		else
		{
			//MyBotModule.Broodwar.printf("Error, no worker found");
		}
	}


	/// �ش� �ϲ� �������κ��� ���� ����� ���� ������ �����մϴ�
	public Unit getClosestEnemyUnitFromWorker(Unit worker)
	{
		if (worker == null) return null;

		Unit closestUnit = null;
		double closestDist = 10000;

		for (Unit unit : MyBotModule.Broodwar.enemy().getUnits())
		{
			double dist = unit.getDistance(worker);

			if ((dist < 400) && (closestUnit == null || (dist < closestDist)))
			{
				closestUnit = unit;
				closestDist = dist;
			}
		}

		return closestUnit;
	}

	/// �ش� �ϲ� ���ֿ��� Combat �ӹ��� �ο��մϴ�
	public void setCombatWorker(Unit worker)
	{
		if (worker == null) return;

		workerData.setWorkerJob(worker, WorkerData.WorkerJob.Combat, (Unit)null);
	}

	/// ��� Combat �ϲ� ���ֿ� ���� �ӹ��� �����մϴ�
	public void stopCombat()
	{
		for (Unit worker : workerData.getWorkers())
		{
			if (worker == null) continue;

			if (workerData.getWorkerJob(worker) == WorkerData.WorkerJob.Combat)
			{
				setMineralWorker(worker);
			}
		}
	}
	
	public void setRepairWorker(Unit worker, Unit unitToRepair)
	{
		workerData.setWorkerJob(worker, WorkerData.WorkerJob.Repair, unitToRepair);
	}

	public void stopRepairing(Unit worker)
	{
		workerData.setWorkerJob(worker, WorkerData.WorkerJob.Idle, (Unit)null);
	}

	/// �ϲ� ���ֵ��� ���¸� �����ϴ� workerData ��ü�� ������Ʈ�մϴ�
	public void onUnitMorph(Unit unit)
	{
		if (unit == null) return;

		// if something morphs into a worker, add it
		if (unit.getType().isWorker() && unit.getPlayer() == MyBotModule.Broodwar.self() && unit.getHitPoints() >= 0)
		{
			workerData.addWorker(unit);
		}

		// if something morphs into a building, it was a worker (Zerg Drone)
		if (unit.getType().isBuilding() && unit.getPlayer() == MyBotModule.Broodwar.self() && unit.getPlayer().getRace() == Race.Zerg)
		{
			// �ش� worker �� workerData ���� �����Ѵ�
			workerData.workerDestroyed(unit);
		}
	}

	/// �ϲ� ���ֵ��� ���¸� �����ϴ� workerData ��ü�� ������Ʈ�մϴ�
	public void onUnitShow(Unit unit)
	{
		if (unit == null) return;

		// add the depot if it exists
		if (unit.getType().isResourceDepot() && unit.getPlayer() == MyBotModule.Broodwar.self())
		{
			workerData.addDepot(unit);
		}

		// add the worker
		if (unit.getType().isWorker() && unit.getPlayer() == MyBotModule.Broodwar.self() && unit.getHitPoints() >= 0)
		{
			workerData.addWorker(unit);
		}

		if (unit.getType().isResourceDepot() && unit.getPlayer() == MyBotModule.Broodwar.self())
		{
			rebalanceWorkers();
		}

	}

	// ���ϰ��ִ� resource depot �� ����� ���� mineral worker ���� �����Ǿ� �ִٸ�, idle ���·� �����
	// idle worker ���� mineral job �� �ο��� ��, mineral worker �� ������ resource depot ���� �̵��ϰ� �ȴ�  
	public void rebalanceWorkers()
	{
		for (Unit worker : workerData.getWorkers())
		{
			if (workerData.getWorkerJob(worker) != WorkerData.WorkerJob.Minerals)
			{
				continue;
			}

			Unit depot = workerData.getWorkerDepot(worker);

			if (depot != null && workerData.depotHasEnoughMineralWorkers(depot))
			{
				workerData.setWorkerJob(worker, WorkerData.WorkerJob.Idle, (Unit)null);
			}
			else if (depot == null)
			{
				workerData.setWorkerJob(worker, WorkerData.WorkerJob.Idle, (Unit)null);
			}
		}
	}

	/// �ϲ� ���ֵ��� ���¸� �����ϴ� workerData ��ü�� ������Ʈ�մϴ�
	public void onUnitDestroy(Unit unit) 
	{
		if (unit == null) return;

		if (unit.getType().isResourceDepot() && unit.getPlayer() == MyBotModule.Broodwar.self())
		{
			workerData.removeDepot(unit);
		}

		if (unit.getType().isWorker() && unit.getPlayer() == MyBotModule.Broodwar.self()) 
		{
			workerData.workerDestroyed(unit);
		}

		if (unit.getType() == UnitType.Resource_Mineral_Field)
		{
			rebalanceWorkers();
		}
	}

	public boolean isMineralWorker(Unit worker)
	{
		if (worker == null) return false;

		return workerData.getWorkerJob(worker) == WorkerData.WorkerJob.Minerals || workerData.getWorkerJob(worker) == WorkerData.WorkerJob.Idle;
	}

	public boolean isScoutWorker(Unit worker)
	{
		if (worker == null) return false;

		return (workerData.getWorkerJob(worker) == WorkerData.WorkerJob.Scout);
	}

	public boolean isConstructionWorker(Unit worker)
	{
		if (worker == null) return false;

		return (workerData.getWorkerJob(worker) == WorkerData.WorkerJob.Build);
	}

	public int getNumMineralWorkers() 
	{
		return workerData.getNumMineralWorkers();	
	}

	/// idle ������ �ϲ� ���� unit �� ���ڸ� �����մϴ�
	public int getNumIdleWorkers() 
	{
		return workerData.getNumIdleWorkers();	
	}

	public int getNumGasWorkers() 
	{
		return workerData.getNumGasWorkers();
	}

	/// �ϲ� ���ֵ��� ���¸� �����ϴ� workerData ��ü�� �����մϴ�
	public WorkerData getWorkerData()
	{
		return workerData;
	}
}