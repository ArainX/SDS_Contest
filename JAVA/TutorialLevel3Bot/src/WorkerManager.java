import java.util.HashMap;
import java.util.Map;

import bwapi.Race;
import bwapi.Unit;
import bwapi.UnitType;
import bwta.BWTA;
import bwta.BaseLocation;

public class WorkerManager {

	// Worker ~ Mineral Field �� assign ���踦 �����ϴ� map
	private Map<Integer, Unit> workerMineralAssignment = new HashMap<Integer, Unit>();

	// ������ Mineral Field �� assign �� Worker ���� �� �����ϴ� map
	private Map<Integer, Integer> workerCountOnMineral = new HashMap<Integer, Integer>();

	private static WorkerManager instance = new WorkerManager();
	
	public static WorkerManager Instance() {
		return instance;
	}
	
	public WorkerManager() {
		for (Unit unit : MyBotModule.Broodwar.getAllUnits())
		{
			if ((unit.getType() == UnitType.Resource_Mineral_Field))
			{
				workerCountOnMineral.put(unit.getID(), new Integer(0));
			}
		}
	}
	
	public void update() {
		
		updateWorkers1();
		
		//updateWorkers2();	

		//updateWorkers3();		
	}

	public void updateWorkers1() {
		for (Unit unit : MyBotModule.Broodwar.self().getUnits()){

			if (unit == null) continue;

			if (unit.getType().isWorker()) {

				// unit ���κ��� ���� �����̿� �ִ� Mineral �� ã��, �� Mineral �� Right Click �� �Ѵ�
				Unit closestMineral = getClosestMineralFrom(unit);

				if (closestMineral != null) {
					System.out.println("closestMineral from " + unit.getType()+ " " + unit.getID()
						+ " is " + closestMineral.getType() + " " + closestMineral.getID() 
						+ " at " + closestMineral.getTilePosition().getX() + "," + closestMineral.getTilePosition().getY());

					// �� frame ���� Right Click �� �ϴ� ���̹Ƿ�, �ᱹ �ش� unit �� �ƹ� �ϵ� ���ϰ� �ȴ�. 
					unit.gather(closestMineral);
				}
			}
		}
	}
	
	public Unit getClosestMineralFrom(Unit worker)
	{
		if (worker == null) return null;

		// worker���κ��� ���� ����� BaseLocation�� ã�´�
		// BWTA.BaseLocation = �� �󿡼� Mineral / Gas Geyser ���� ������ �־ Terran_Command_Center, Protoss_Nexus, Zerg_Hatchery �� �Ǽ��ϱ⿡ ������ ����
		BaseLocation closestBaseLocation = null;
		double closestDistance = 1000000000;

		for (BaseLocation baseLocation : BWTA.getBaseLocations()){

			if (baseLocation == null) continue;

			double distance = worker.getDistance(baseLocation.getPosition());

			if (distance < closestDistance)
			{
				closestBaseLocation = baseLocation;
				closestDistance = distance;
			}
		}

		if (closestBaseLocation == null) {
			return null;
		}
		
		System.out.println("closestBaseLocation from " + worker.getType() + " " + worker.getID()
			+ " is " + closestBaseLocation.getTilePosition().getX() + "," + closestBaseLocation.getTilePosition().getY());

		// �ش� BaseLocation �� Mineral �� �߿��� worker���� ���� ����� Mineral �� ã�´�
		Unit closestMineral = null;
		closestDistance = 1000000000;

		//BaseLocation.getMinerals() . ��ο� ������ ������, null �� ����
		//BaseLocation.getStaticMinerals() . ��ο� ������ ������, UnitTypes.Unknown �� ����
		for (Unit mineral : closestBaseLocation.getMinerals()){
			if (mineral == null) continue;

			double distance = worker.getDistance(mineral.getPosition());

			if (distance < closestDistance)
			{
				closestMineral = mineral;
				closestDistance = distance;
			}
		}

		return closestMineral;		
	}
	
	public void updateWorkers2() {
		for (Unit unit : MyBotModule.Broodwar.self().getUnits()){

			if (unit == null) continue;

			if (unit.getType().isWorker()) {

				if (unit.isIdle()) {
					System.out.println(unit.getType() + " " + unit.getID() + " is idle");
				}

				// unit �� idle �����̰�, ź���� �����̸� 
				if (unit.isIdle() && unit.isCompleted())
				{
					System.out.println(unit.getType() + " " + unit.getID() + " is ready to command");

					// unit ���κ��� ���� �����̿� �ִ� Mineral �� ã��, �� Mineral �� Right Click �� �Ѵ�
					Unit closestMineral = getClosestMineralFrom(unit);
	
					if (closestMineral != null) {
						System.out.println("closestMineral from " + unit.getType()+ " " + unit.getID()
							+ " is " + closestMineral.getType() + " " + closestMineral.getID() 
							+ " at " + closestMineral.getTilePosition().getX() + "," + closestMineral.getTilePosition().getY());
	
						// �� frame ���� Right Click �� �ϴ� ���̹Ƿ�, �ᱹ �ش� unit �� �ƹ� �ϵ� ���ϰ� �ȴ�. 
						unit.gather(closestMineral);
					}
				}
			}
		}
	}
	

	public void updateWorkers3()
	{
		// ������ Worker �� ���ؼ�
		// ���� ����� �Ʊ� ResourceDepot ��ó��, ���� ����� Mineral �� ä���ϵ��� �ϵ� (�Ÿ� ��� �ʿ�)
		// Worker ���� ���� Mineral �� �л�ǵ��� �Ѵ� (������ Mineral �� �Ҵ�� worker ���� ���ڸ� ���� / �ֽ�ȭ �ؾ� �Ѵ�)
	
		// worker �� ���� ���� ���� idle ���°� ������, ���� �����ϴ� ���߿��� ��� idle ���°� �ȴ�
		for (Unit unit : MyBotModule.Broodwar.self().getUnits()){
	
			if (unit == null) continue;
			
			if (unit.getType().isWorker()) {
	
				// unit �� idle �����̰�, ź���� �����̸� 
				if (unit.isIdle() && unit.isCompleted())
				{
					System.out.println(unit.getType() + " " + unit.getID() + " is idle");
	
					// unit ���� ������ Mineral �� ã��, �� Mineral �� Right Click �� �Ѵ�
					Unit bestMineral = getBestMineralTo(unit);
	
					if (bestMineral != null) {
						System.out.println("bestMineral from " + unit.getType() + " " + unit.getID()
							+ " is " + bestMineral.getType() + " " + bestMineral.getID() 
							+ " at " + bestMineral.getTilePosition().getX() + "," + bestMineral.getTilePosition().getY());
	
						unit.gather(bestMineral);
	
						// unit �� Mineral �� assign ������ ������Ʈ�Ѵ�
						workerMineralAssignment.put(new Integer(unit.getID()), bestMineral);
						// Mineral �� assigned unit ���ڸ� ������Ʈ�Ѵ�
						increaseWorkerCountOnMineral(bestMineral, 1);
					}
				}
			}
		}
	
		// Mineral �� assigned unit ���ڸ� ȭ�鿡 ǥ��
		for (Integer i : workerMineralAssignment.keySet()) {
			Unit mineral = workerMineralAssignment.get(i);
			if (workerCountOnMineral.containsKey(new Integer(mineral.getID()))) {
				MyBotModule.Broodwar.drawTextMap(mineral.getPosition().getX(), mineral.getPosition().getY() + 12, 
						"worker: " + workerCountOnMineral.get(mineral.getID()));
			}
		}

		/*
		// Worker �� Mineral ������ �ֿܼ� ǥ��
		System.out.println("\nworkerMineralAssignment size " + workerMineralAssignment.size());
		for (Integer i : workerMineralAssignment.keySet()) {
			System.out.println("worker " + i + " ~ mineral " + workerMineralAssignment.get(i).getID());
		}		
		*/		

	}
	
	public Unit getBestMineralTo(Unit worker)
	{
		if (worker == null) return null;
	
		// worker���κ��� ���� ����� BaseLocation�� ã�´�
		BaseLocation closestBaseLocation = null;
		// 128 * 128 Ÿ�ϻ������� �ʿ��� ���� �� �Ÿ��� sqrt(128 * 32  * 128 * 32 + 128 * 32 * 128 * 32) = 5792.6 point 
		double closestDistance = 1000000000;
	
		for (BaseLocation baseLocation : BWTA.getBaseLocations()){
	
			if (baseLocation == null) continue;
	
			double distance = worker.getDistance(baseLocation.getPosition());
	
			if (distance < closestDistance)
			{
				closestBaseLocation = baseLocation;
				closestDistance = distance;
			}
		}
	
		if (closestBaseLocation == null) {
			return null;
		}
		
		System.out.println("closestBaseLocation from " + worker.getType() + " " + worker.getID()
			+ " is " + closestBaseLocation.getTilePosition().getX() + "," + closestBaseLocation.getTilePosition().getY());
	
		// �ش� BaseLocation �� Mineral �� �߿��� worker �� ���� ���� �����Ǿ��ִ� ��, ���߿����� BaseLocation ���κ��� ���� ����� ���� ã�´�
		Unit bestMineral = null;
		double bestDistance = 1000000000;
		int bestNumAssigned = 1000000000;
	
		//BaseLocation.getMinerals() . ��ο� ������ ������, null �� ����
		//BaseLocation.getStaticMinerals() . ��ο� ������ ������, UnitTypes.Unknown �� ����
		for (Unit mineral : closestBaseLocation.getMinerals()){
			if (mineral == null) continue;
	
			// �ش� Mineral �� ������ worker ����
			int numAssigned = 0;
			if (workerCountOnMineral.containsKey(new Integer(mineral.getID())) ){
				Integer n = workerCountOnMineral.get(new Integer(mineral.getID()));
				numAssigned = n.intValue();
			}
			// �ش� Mineral �� BaseLocation ���� �Ÿ�
			double dist = mineral.getDistance(closestBaseLocation.getPosition());
	
			if (numAssigned < bestNumAssigned)
			{
				bestMineral = mineral;
				bestDistance = dist;
				bestNumAssigned = numAssigned;
			}
			else if (numAssigned == bestNumAssigned)
			{
				if (dist < bestDistance)
				{
					bestMineral = mineral;
					bestDistance = dist;
					bestNumAssigned = numAssigned;
				}
			}
		}
	
		return bestMineral;
	}
	
	public void increaseWorkerCountOnMineral(Unit mineral, int num)
	{
		// Mineral �� assign �� worker ���ڸ� �����Ѵ�
		if (workerCountOnMineral.containsKey(new Integer(mineral.getID())))
		{
			workerCountOnMineral.replace(new Integer(mineral.getID()), new Integer(workerCountOnMineral.get(mineral.getID()) + num));
		}
		else
		{
			workerCountOnMineral.put(new Integer(mineral.getID()), new Integer(num));
		}
	}
	
	public void onUnitDestroy(Unit unit)
	{
		if (unit == null) return;
	
		if (unit.getType().isWorker() && unit.getPlayer() == MyBotModule.Broodwar.self()) 
		{
			// �ش� �ϲ۰� Mineral �� assign ������ �����Ѵ�
			System.out.println("removeWorker " + unit.getID() + " from Mineral Worker ");
			increaseWorkerCountOnMineral(workerMineralAssignment.get(unit.getID()), -1);
			workerMineralAssignment.remove(unit.getID());
		}
	}
	
	public void onUnitMorph(Unit unit)
	{
		if (unit == null) return;
	
		// ���� ���� �ϲ��� �ǹ��� morph �� ���
		if (unit.getPlayer().getRace() == Race.Zerg && unit.getPlayer() == MyBotModule.Broodwar.self() && unit.getType().isBuilding())
		{
			// �ش� �ϲ۰� Mineral �� assign ������ �����Ѵ�
			System.out.println("removeWorker " + unit.getID() + " from Mineral Worker ");				
			increaseWorkerCountOnMineral(workerMineralAssignment.get(unit.getID()), -1);
			workerMineralAssignment.remove(unit.getID());
		}
	}
	

	
}