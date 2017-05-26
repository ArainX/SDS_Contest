import bwapi.Player;
import bwapi.TilePosition;
import bwapi.Unit;

/// ���� �����α׷��� ��ü�� �Ǵ� class
/// ��Ÿũ����Ʈ ��� ���� �߻��ϴ� �̺�Ʈ���� �����ϰ� ó���ǵ��� �ش� Manager ��ü���� �̺�Ʈ�� �����ϴ� ������ Controller ������ �մϴ�
public class GameCommander {

	// ������ �÷��� : ���� Manager �� ��𿡼� ������ ����Ű���� �˱� ���� 
	private boolean isToLogConsole = false;
	// ������ �÷��� : ���� Manager �� ��𿡼� �ð��� ���� ������ �˱� ���� 
	private boolean isToCheckTime = false;
	
	private long timeStarted = 0;
	private long timeFinished = 0;
	private long timeElapsedInInformationManager = 0;
	private long timeElapsedInMapGrid = 0;
	private long timeElapsedInWorkerManager = 0;
	private long timeElapsedInBuildManager = 0;
	private long timeElapsedInConstructionManager = 0;
	private long timeElapsedInScoutManager = 0;
	private long timeElapsedInStrategyManager = 0;
	private long timeElapsedInUXManager = 0;
	private long timeElapsedTotal = 0;
	private long timeAccumulatedInInformationManager = 0;
	private long timeAccumulatedInMapGrid = 0;
	private long timeAccumulatedInWorkerManager = 0;
	private long timeAccumulatedInBuildManager = 0;
	private long timeAccumulatedInConstructionManager = 0;
	private long timeAccumulatedInScoutManager = 0;
	private long timeAccumulatedInStrategyManager = 0;
	private long timeAccumulatedInUXManager = 0;
	private long timeAccumulatedTotal = 0;
	
	/// ��Ⱑ ���۵� �� ��ȸ������ �߻��ϴ� �̺�Ʈ�� ó���մϴ�
	public void onStart() 
	{
		TilePosition startLocation = MyBotModule.Broodwar.self().getStartLocation();
		if (startLocation == TilePosition.None || startLocation == TilePosition.Unknown) {
			return;
		}
		StrategyManager.Instance().onStart();
	}

	/// ��Ⱑ ����� �� ��ȸ������ �߻��ϴ� �̺�Ʈ�� ó���մϴ�
	public void onEnd(boolean isWinner)
	{
		StrategyManager.Instance().onEnd(isWinner);
	}

	/// ��� ���� �� �� �����Ӹ��� �߻��ϴ� �̺�Ʈ�� ó���մϴ�
	public void onFrame()
	{
		if (MyBotModule.Broodwar.isPaused()) {
			return;
		}

		try {
			if ( isToLogConsole) System.out.print("\n(a");

			// �Ʊ� ���̽� ��ġ. ���� ���̽� ��ġ. �� ���ֵ��� �������� ���� Map �ڷᱸ���� ����/������Ʈ
			if (isToCheckTime) timeStarted = System.currentTimeMillis();
			InformationManager.Instance().update();
			if (isToCheckTime) timeFinished = System.currentTimeMillis();
			if (isToCheckTime) timeElapsedInInformationManager = (timeFinished - timeStarted);

			if ( isToLogConsole) System.out.print("b");
		
			// �� ������ ��ġ�� ��ü MapGrid �ڷᱸ���� ����
			if (isToCheckTime) timeStarted = System.currentTimeMillis();
			MapGrid.Instance().update();
			if (isToCheckTime) timeFinished = System.currentTimeMillis();
			if (isToCheckTime) timeElapsedInMapGrid = (timeFinished - timeStarted);

			if ( isToLogConsole) System.out.print("c");

			// economy and base managers
			// �ϲ� ���ֿ� ���� ��� (�ڿ� ä��, �̵� ����) ���� �� ����
			if (isToCheckTime) timeStarted = System.currentTimeMillis();
			WorkerManager.Instance().update();
			if (isToCheckTime) timeFinished = System.currentTimeMillis();
			if (isToCheckTime) timeElapsedInWorkerManager = (timeFinished - timeStarted);

			if ( isToLogConsole) System.out.print("d");

			// �������ť�� �����ϸ�, ��������� ���� ���� ����(���� �Ʒ�, ��ũ ���׷��̵� ��)�� �����Ѵ�.
			if (isToCheckTime) timeStarted = System.currentTimeMillis();
			BuildManager.Instance().update();
			if (isToCheckTime) timeFinished = System.currentTimeMillis();
			if (isToCheckTime) timeElapsedInBuildManager = (timeFinished - timeStarted);

			if ( isToLogConsole) System.out.print("e");

			// ������� �� �ǹ� ���忡 ���ؼ���, �ϲ����� ����, ��ġ����, �Ǽ� �ǽ�, �ߴܵ� �ǹ� ���� �簳�� �����Ѵ�
			if (isToCheckTime) timeStarted = System.currentTimeMillis();
			ConstructionManager.Instance().update();
			if (isToCheckTime) timeFinished = System.currentTimeMillis();
			if (isToCheckTime) timeElapsedInConstructionManager = (timeFinished - timeStarted);

			if ( isToLogConsole) System.out.print("f");

			// ���� �ʱ� ���� ���� ���� �� ���� ���� ��Ʈ���� �����Ѵ�
			if (isToCheckTime) timeStarted = System.currentTimeMillis();
			ScoutManager.Instance().update();
			if (isToCheckTime) timeFinished = System.currentTimeMillis();
			if (isToCheckTime) timeElapsedInScoutManager = (timeFinished - timeStarted);

			if ( isToLogConsole) System.out.print("g");

			// ������ �Ǵ� �� ���� ��Ʈ��
			if (isToCheckTime) timeStarted = System.currentTimeMillis();
			StrategyManager.Instance().update();
			if (isToCheckTime) timeFinished = System.currentTimeMillis();
			if (isToCheckTime) timeElapsedInStrategyManager = (timeFinished - timeStarted);

			if ( isToLogConsole) System.out.print("h");

			// ȭ�� ��� �� ����� �Է� ó��
			if (isToCheckTime) timeStarted = System.currentTimeMillis();
			UXManager.Instance().update();
			if (isToCheckTime) timeFinished = System.currentTimeMillis();
			if (isToCheckTime) timeElapsedInUXManager = (timeFinished - timeStarted);

			if ( isToLogConsole) System.out.print("i)");

			if (isToCheckTime) {				
				timeAccumulatedInInformationManager += timeElapsedInInformationManager;
				timeAccumulatedInMapGrid += timeElapsedInMapGrid;
				timeAccumulatedInWorkerManager += timeElapsedInWorkerManager;
				timeAccumulatedInBuildManager += timeElapsedInBuildManager;
				timeAccumulatedInConstructionManager += timeElapsedInConstructionManager;
				timeAccumulatedInScoutManager += timeElapsedInScoutManager;
				timeAccumulatedInStrategyManager += timeElapsedInStrategyManager;
				timeAccumulatedInUXManager += timeElapsedInUXManager;
	
				timeElapsedTotal = timeElapsedInInformationManager + timeElapsedInMapGrid + timeElapsedInWorkerManager
						+ timeElapsedInBuildManager + timeElapsedInConstructionManager + timeElapsedInScoutManager
						+ timeElapsedInStrategyManager + timeElapsedInUXManager;
				timeAccumulatedTotal += timeElapsedTotal;

				drawGameCommanderTimeConsumeScreen(400, 200);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}		
	}


	/// ����(�ǹ�/��������/��������)�� Create �� �� �߻��ϴ� �̺�Ʈ�� ó���մϴ�
	public void onUnitCreate(Unit unit) { 
		InformationManager.Instance().onUnitCreate(unit);
	}

	///  ����(�ǹ�/��������/��������)�� Destroy �� �� �߻��ϴ� �̺�Ʈ�� ó���մϴ�
	public void onUnitDestroy(Unit unit) {
		// ResourceDepot �� Worker �� ���� ó��
		WorkerManager.Instance().onUnitDestroy(unit);

		InformationManager.Instance().onUnitDestroy(unit); 
	}
	
	/// ����(�ǹ�/��������/��������)�� Morph �� �� �߻��ϴ� �̺�Ʈ�� ó���մϴ�
	/// Zerg ������ ������ �ǹ� �Ǽ��̳� ��������/�������� ���꿡�� ���� ��κ� Morph ���·� ����˴ϴ�
	public void onUnitMorph(Unit unit) { 
		InformationManager.Instance().onUnitMorph(unit);

		// Zerg ���� Worker �� Morph �� ���� ó��
		WorkerManager.Instance().onUnitMorph(unit);
	}

	/// ����(�ǹ�/��������/��������)�� �Ҽ� �÷��̾ �ٲ� �� �߻��ϴ� �̺�Ʈ�� ó���մϴ�
	/// Gas Geyser�� � �÷��̾ Refinery �ǹ��� �Ǽ����� ��, Refinery �ǹ��� �ı��Ǿ��� ��, Protoss ���� Dark Archon �� Mind Control �� ���� �Ҽ� �÷��̾ �ٲ� �� �߻��մϴ�
	public void onUnitRenegade(Unit unit) {
		// Vespene_Geyser (���� ����) �� �������� �Ǽ��� ���� ���
		//MyBotModule.Broodwar.sendText("A %s [%p] has renegaded. It is now owned by %s", unit.getType().c_str(), unit, unit.getPlayer().getName().c_str());

		InformationManager.Instance().onUnitRenegade(unit);
	}

	/// ����(�ǹ�/��������/��������)�� �ϴ� �� (�ǹ� �Ǽ�, ���׷��̵�, �������� �Ʒ� ��)�� ������ �� �߻��ϴ� �̺�Ʈ�� ó���մϴ�
	public void onUnitComplete(Unit unit)
	{
		InformationManager.Instance().onUnitComplete(unit);
	}
	
	/// ����(�ǹ�/��������/��������)�� Discover �� �� �߻��ϴ� �̺�Ʈ�� ó���մϴ�
	/// �Ʊ� ������ Create �Ǿ��� �� ��簡, ���� ������ Discover �Ǿ��� �� �߻��մϴ�
	public void onUnitDiscover(Unit unit) {
	}

	/// ����(�ǹ�/��������/��������)�� Evade �� �� �߻��ϴ� �̺�Ʈ�� ó���մϴ�
	/// ������ Destroy �� �� �߻��մϴ�
	public void onUnitEvade(Unit unit) {
	}	

	/// ����(�ǹ�/��������/��������)�� Show �� �� �߻��ϴ� �̺�Ʈ�� ó���մϴ�
	/// �Ʊ� ������ Create �Ǿ��� �� ��簡, ���� ������ Discover �Ǿ��� �� �߻��մϴ�
	public void onUnitShow(Unit unit) { 
		InformationManager.Instance().onUnitShow(unit); 

		// ResourceDepot �� Worker �� ���� ó��
		WorkerManager.Instance().onUnitShow(unit);
	}

	/// ����(�ǹ�/��������/��������)�� Hide �� �� �߻��ϴ� �̺�Ʈ�� ó���մϴ�
	/// ���̴� ������ Hide �� �� �߻��մϴ�
	public void onUnitHide(Unit unit) {
		InformationManager.Instance().onUnitHide(unit); 
	}
	
	/// �ؽ�Ʈ�� �Է� �� ���͸� �Ͽ� �ٸ� �÷��̾�鿡�� �ؽ�Ʈ�� �����Ϸ� �� �� �߻��ϴ� �̺�Ʈ�� ó���մϴ�
	public void onSendText(String text){
	}

	/// �ٸ� �÷��̾�κ��� �ؽ�Ʈ�� ���޹޾��� �� �߻��ϴ� �̺�Ʈ�� ó���մϴ�
	public void onReceiveText(Player player, String text){
	}
	
	public void drawGameCommanderTimeConsumeScreen(int x, int y) {
		MyBotModule.Broodwar.drawTextScreen(x, y, "<Time Consume>");
		y += 10;
		MyBotModule.Broodwar.drawTextScreen(x, y, "Manager");
		MyBotModule.Broodwar.drawTextScreen(x+70, y, "Accumulated");
		MyBotModule.Broodwar.drawTextScreen(x+140, y, "In Frame");
		y += 10;
		MyBotModule.Broodwar.drawTextScreen(x, y, "Information");
		MyBotModule.Broodwar.drawTextScreen(x+70, y, (int)(timeAccumulatedInInformationManager*100/timeAccumulatedTotal)+"%%");
		MyBotModule.Broodwar.drawTextScreen(x+140, y, (int)(timeElapsedInInformationManager*100/timeElapsedTotal)+"%%");
		y += 10;
		MyBotModule.Broodwar.drawTextScreen(x, y, "Worker");
		MyBotModule.Broodwar.drawTextScreen(x+70, y, (int)(timeAccumulatedInWorkerManager*100/timeAccumulatedTotal)+"%%");
		MyBotModule.Broodwar.drawTextScreen(x+140, y, (int)(timeElapsedInWorkerManager*100/timeElapsedTotal)+"%%");
		y += 10;
		MyBotModule.Broodwar.drawTextScreen(x, y, "Build");
		MyBotModule.Broodwar.drawTextScreen(x+70, y, (int)(timeAccumulatedInBuildManager*100/timeAccumulatedTotal)+"%%");
		MyBotModule.Broodwar.drawTextScreen(x+140, y, (int)(timeElapsedInBuildManager*100/timeElapsedTotal)+"%%");
		y += 10;
		MyBotModule.Broodwar.drawTextScreen(x, y, "Construction");
		MyBotModule.Broodwar.drawTextScreen(x+70, y, (int)(timeAccumulatedInConstructionManager*100/timeAccumulatedTotal)+"%%");
		MyBotModule.Broodwar.drawTextScreen(x+140, y, (int)(timeElapsedInConstructionManager*100/timeElapsedTotal)+"%%");
		y += 10;
		MyBotModule.Broodwar.drawTextScreen(x, y, "Scout");
		MyBotModule.Broodwar.drawTextScreen(x+70, y, (int)(timeAccumulatedInScoutManager*100/timeAccumulatedTotal)+"%%");
		MyBotModule.Broodwar.drawTextScreen(x+140, y, (int)(timeElapsedInScoutManager*100/timeElapsedTotal)+"%%");
		y += 10;
		MyBotModule.Broodwar.drawTextScreen(x, y, "Strategy");
		MyBotModule.Broodwar.drawTextScreen(x+70, y, (int)(timeAccumulatedInStrategyManager*100/timeAccumulatedTotal)+"%%");
		MyBotModule.Broodwar.drawTextScreen(x+140, y, (int)(timeElapsedInStrategyManager*100/timeElapsedTotal)+"%%");
		y += 10;
		MyBotModule.Broodwar.drawTextScreen(x, y, "UX");
		MyBotModule.Broodwar.drawTextScreen(x+70, y, (int)(timeAccumulatedInUXManager*100/timeAccumulatedTotal)+"%%");
		MyBotModule.Broodwar.drawTextScreen(x+140, y, (int)(timeElapsedInUXManager*100/timeElapsedTotal)+"%%");	
	}

}