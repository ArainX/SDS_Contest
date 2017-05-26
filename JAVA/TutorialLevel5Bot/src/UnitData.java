import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import bwapi.Color;
import bwapi.Unit;
import bwapi.UnitType;

public class UnitData {

	/// Unit �� UnitInfo �� Map ���·� �����ϴ� �ڷᱸ�� 
	Map<Unit,UnitInfo> unitAndUnitInfoMap = new HashMap<Unit,UnitInfo>();
	
	/// UnitType�� �ı�/����� ���� ���� ������
	Map<String,Integer> numDeadUnits = new HashMap<String,Integer>();
	
	/// UnitType�� �Ǽ�/�Ʒ��ߴ� ���� ���� ������
	Map<String,Integer> numCreatedUnits = new HashMap<String,Integer>();
	
	/// UnitType�� �����ϴ� ���� ���� ī��Ʈ. ���� ������ ��� �ĺ��� ���� ���� ī��Ʈ
	Map<String,Integer> numUnits = new HashMap<String,Integer>();
	
	/// ����� ������ �����ϴµ� �ҿ�Ǿ��� Mineral �� ������ (�󸶳� ���ظ� ���Ҵ°� ����ϱ� ������)
	private int mineralsLost = 0;
	/// ����� ������ �����ϴµ� �ҿ�Ǿ��� Gas �� ������ (�󸶳� ���ظ� ���Ҵ°� ����ϱ� ������)
	private int gasLost = 0;

	public UnitData() 
	{
		/*
		int maxTypeID = 220;
		for (final UnitType t : UnitType.allUnitTypes())
		{
			maxTypeID = maxTypeID > t.getID() ? maxTypeID : t.getID();
		}
		
		Map<String,Integer> numDeadUnits = new HashMap<String,Integer>();
		Map<String,Integer> numCreatedUnits = new HashMap<String,Integer>();
		Map<String,Integer> numUnits = new HashMap<String,Integer>();
		 */
	}

	/// ������ ���������� ������Ʈ�մϴ�
	public void updateUnitInfo(Unit unit)
	{
		if (unit == null) { return; }

		boolean firstSeen = false;
		if (!unitAndUnitInfoMap.containsKey(unit))
		{
			firstSeen = true;
			unitAndUnitInfoMap.put(unit, new UnitInfo());
		}

		UnitInfo ui   = new UnitInfo();
		ui.setUnit(unit);
		ui.setPlayer(unit.getPlayer());
		ui.setLastPosition(unit.getPosition());
		ui.setLastHealth(unit.getHitPoints());
		ui.setLastShields(unit.getShields());
		ui.setUnitID(unit.getID());
		ui.setType(unit.getType());
		ui.setCompleted(unit.isCompleted());
		
		unitAndUnitInfoMap.put(unit, ui);
		
		if (firstSeen)
		{
			if(!numCreatedUnits.containsKey(unit.getType().toString())){
				numCreatedUnits.put(unit.getType().toString(), 1);
			}else{
				numCreatedUnits.put(unit.getType().toString(), numCreatedUnits.get(unit.getType().toString()) + 1);
			}
			if(!numUnits.containsKey(unit.getType().toString())){
				numUnits.put(unit.getType().toString(), 1);
			}else{
				numUnits.put(unit.getType().toString(), numUnits.get(unit.getType().toString()) + 1);
			}
			//numCreatedUnits[unit.getType().getID()]++;
			//numUnits[unit.getType().getID()]++;
		}
	}

	/// �ı�/����� ������ �ڷᱸ������ �����մϴ�
	public void removeUnit(Unit unit)
	{
		if (unit == null) { return; }

		mineralsLost += unit.getType().mineralPrice();
		gasLost += unit.getType().gasPrice();
		if(numUnits.get(unit.getType().toString()) == 1){
			numUnits.remove(unit.getType().toString());
		}else{
			numUnits.put(unit.getType().toString(), numUnits.get(unit.getType().toString()) - 1);
		}
		if(!numDeadUnits.containsKey(unit.getType().toString())){
			numDeadUnits.put(unit.getType().toString(), 1);
		}else{
			numDeadUnits.put(unit.getType().toString(), numDeadUnits.get(unit.getType().toString()) + 1);
		}
		// numUnits[unit.getType().getID()]--;
		// numDeadUnits[unit.getType().getID()]++;

		unitAndUnitInfoMap.remove(unit);
	}

	/// �����Ͱ� null �� �Ǿ��ų�, �ı��Ǿ� Resource_Vespene_Geyser�� ���ư� Refinery, �������� �ǹ��� �־��� �ɷ� �����صξ��µ� ������ �ı��Ǿ� ������ �ǹ� (Ư��, �׶��� ��� ��Ÿ�� �Ҹ��� �ǹ�) �����͸� �����մϴ�
	public void removeBadUnits()
	{
		Iterator<Unit> it = unitAndUnitInfoMap.keySet().iterator();
		while(it.hasNext())
		{
			Unit unit = it.next();
			if (isBadUnitInfo(unitAndUnitInfoMap.get(unit)))
			{
				if(numUnits.get(unit.getType().toString()) != null)
				{
					numUnits.put(unit.getType().toString(), numUnits.get(unit.getType().toString()) - 1);
				}
			}
		}
	}

	public final boolean isBadUnitInfo(final UnitInfo ui)
	{
		if (ui.getUnit() == null)
		{
			return false;
		}

		// Cull away any refineries/assimilators/extractors that were destroyed and reverted to vespene geysers
		if (ui.getUnit().getType() == UnitType.Resource_Vespene_Geyser)
		{ 
			return true;
		}

		// If the unit is a building and we can currently see its position and it is not there
		if (ui.getType().isBuilding() && MyBotModule.Broodwar.isVisible(ui.getLastPosition().getX()/32, ui.getLastPosition().getY()/32) && !ui.getUnit().isVisible())
		{
			return true;
		}

		return false;
	}

	/// ����� ������ �����ϴµ� �ҿ�Ǿ��� Gas �� ������ (�󸶳� ���ظ� ���Ҵ°� ����ϱ� ������)
	public final int getGasLost() 
	{ 
		return gasLost; 
	}

	/// ����� ������ �����ϴµ� �ҿ�Ǿ��� Mineral �� ������ (�󸶳� ���ظ� ���Ҵ°� ����ϱ� ������) �� �����մϴ�
	public final int getMineralsLost()
	{ 
		return mineralsLost; 
	}

	/// �ش� UnitType �� �ĺ��� Unit ���ڸ� �����մϴ�
	public final int getNumUnits(String t) 
	{ 
		if(numUnits.get(t.toString()) != null)
		{
			return numUnits.get(t.toString());
		}
		else
		{
			return 0;
		}
	}

	/// �ش� UnitType �� �ĺ��� Unit �ı�/��� �������� �����մϴ�
	public final int getNumDeadUnits(String t)
	{ 
		if(numDeadUnits.get(t.toString()) != null)
		{
			return numDeadUnits.get(t.toString());
		}
		else
		{
			return 0;
		}
	}

	/// �ش� UnitType �� �ĺ��� Unit �Ǽ�/�Ʒ� �������� �����մϴ�
	public final int getNumCreatedUnits(String t)
	{
		if(numCreatedUnits.get(t.toString()) != null)
		{
			return numCreatedUnits.get(t.toString());
		}
		else
		{
			return 0;
		}
	}

	public final Map<Unit,UnitInfo> getUnits() 
	{ 
		return unitAndUnitInfoMap; 
	}

	public Map<String, Integer> getNumDeadUnits() {
		return numDeadUnits;
	}

	public Map<String, Integer> getNumCreatedUnits() {
		return numCreatedUnits;
	}

	public Map<String, Integer> getNumUnits() {
		return numUnits;
	}
}