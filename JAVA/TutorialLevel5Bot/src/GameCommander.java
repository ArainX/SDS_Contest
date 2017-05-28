import bwapi.Player;
import bwapi.TilePosition;
import bwapi.Unit;

/// ���� �����α׷��� ��ü�� �Ǵ� class
/// ��Ÿũ����Ʈ ��� ���� �߻��ϴ� �̺�Ʈ���� �����ϰ� ó���ǵ��� �ش� Manager ��ü���� �̺�Ʈ�� �����ϴ� ������ Controller ������ �մϴ�
public class GameCommander {

	// ������ �÷��� : ���� Manager �� ��𿡼� ������ ����Ű���� �˱� ���� 
	private boolean isToFindError = false;
	
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
			if ( isToFindError) System.out.print("(a");

			// �Ʊ� ���̽� ��ġ. ���� ���̽� ��ġ. �� ���ֵ��� �������� ���� Map �ڷᱸ���� ����/������Ʈ
			InformationManager.Instance().update();

			if ( isToFindError) System.out.print("b");
		
			// �� ������ ��ġ�� ��ü MapGrid �ڷᱸ���� ����
			MapGrid.Instance().update();

			if ( isToFindError) System.out.print("c");

			// economy and base managers
			// �ϲ� ���ֿ� ���� ��� (�ڿ� ä��, �̵� ����) ���� �� ����
			WorkerManager.Instance().update();

			if ( isToFindError) System.out.print("d");

			// �������ť�� �����ϸ�, ��������� ���� ���� ����(���� �Ʒ�, ��ũ ���׷��̵� ��)�� �����Ѵ�.
			BuildManager.Instance().update();

			if ( isToFindError) System.out.print("e");

			// ������� �� �ǹ� ���忡 ���ؼ���, �ϲ����� ����, ��ġ����, �Ǽ� �ǽ�, �ߴܵ� �ǹ� ���� �簳�� �����Ѵ�
			ConstructionManager.Instance().update();

			if ( isToFindError) System.out.print("f");

			// ���� �ʱ� ���� ���� ���� �� ���� ���� ��Ʈ���� �����Ѵ�
			ScoutManager.Instance().update();

			if ( isToFindError) System.out.print("g");

			// ������ �Ǵ� �� ���� ��Ʈ��
			StrategyManager.Instance().update();

			if ( isToFindError) System.out.print("h)");

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

}