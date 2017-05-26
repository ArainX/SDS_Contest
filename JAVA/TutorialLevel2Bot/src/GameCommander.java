import bwapi.Unit;

public class GameCommander {
	
	public void onStart() 
	{
	}

	public void onEnd(boolean isWinner)
	{
	}

	public void onFrame()
	{
		// �Ʊ� ���̽� ��ġ. ���� ���̽� ��ġ ������ ����/������Ʈ�Ѵ�
		InformationManager.Instance().update();

		// �÷��̾� ���� ǥ�� - InformationManager �� ������� ���
		MyBotModule.Broodwar.drawTextScreen(5, 5, "My Player: "+MyBotModule.Broodwar.self().getTextColor()+MyBotModule.Broodwar.self().getName()
				+" ("+InformationManager.Instance().selfRace+")");
		MyBotModule.Broodwar.drawTextScreen(5, 15, "Enemy Player: "+MyBotModule.Broodwar.enemy().getTextColor()
				+MyBotModule.Broodwar.enemy().getName()+" ("+InformationManager.Instance().enemyRace+")");
		
		// ���� FrameCount ǥ��
		MyBotModule.Broodwar.drawTextScreen(300, 100, "FrameCount: "+MyBotModule.Broodwar.getFrameCount());

		// ���� id ǥ��
		for (Unit unit : MyBotModule.Broodwar.getAllUnits()) {
			MyBotModule.Broodwar.drawTextMap(unit.getPosition().getX(), unit.getPosition().getY(), ""+unit.getID());
		}		
		
		// �÷��̾� Start Location ǥ�� - InformationManager �� ������� ���
		if (InformationManager.Instance().mainBaseLocations.get(MyBotModule.Broodwar.self()) != null) {
			MyBotModule.Broodwar.drawTextScreen(200, 5, "Start Location: "
				+InformationManager.Instance().mainBaseLocations.get(MyBotModule.Broodwar.self()).getTilePosition().getX()+","
				+InformationManager.Instance().mainBaseLocations.get(MyBotModule.Broodwar.self()).getTilePosition().getY());
		}
		if (InformationManager.Instance().mainBaseLocations.get(MyBotModule.Broodwar.enemy()) != null) {
			MyBotModule.Broodwar.drawTextScreen(200, 15, "Start Location: "
				+InformationManager.Instance().mainBaseLocations.get(MyBotModule.Broodwar.enemy()).getTilePosition().getX()+","
				+InformationManager.Instance().mainBaseLocations.get(MyBotModule.Broodwar.enemy()).getTilePosition().getY());
		}
	}

	public void onUnitShow(Unit unit) { 
	}

	public void onUnitHide(Unit unit) {
	}

	public void onUnitCreate(Unit unit) { 
	}

	public void onUnitComplete(Unit unit) {
	}

	public void onUnitDestroy(Unit unit) {
	}

	public void onUnitRenegade(Unit unit) {
	}

	public void onUnitMorph(Unit unit) {
	}

	public void onUnitDiscover(Unit unit) {		
	}

	public void onUnitEvade(Unit unit) {		
	}
}