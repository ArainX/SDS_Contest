import bwapi.Color;
import bwapi.DefaultBWListener;
import bwapi.Game;
import bwapi.Mirror;
import bwapi.Player;
import bwapi.Position;
import bwapi.Unit;
import bwapi.Flag.Enum;
import bwta.BWTA;

/// �����α׷��� �⺻���� ���� ������ ������ class �μ�, ��Ÿũ����Ʈ ��� ���� �߻��ϴ� �̺�Ʈ���� GameCommander class �ν��Ͻ����� �����մϴ�.
/// MyBotModule class�� ������ ���� ����,
/// ���� �����α׷� ������ GameCommander class �� �����ϴ� ���·� �����ϵ��� �մϴ�
/// @see GameCommander
public class MyBotModule extends DefaultBWListener {

	/// BWAPI �� �ش��ϴ� ���� ��ü
	private Mirror mirror = new Mirror();
	
	/// ��Ÿũ����Ʈ ��� ��Ȳ ��ü�� ���� ��Ȳ �ľ� �� �׼� ������ �����ϴ� ��ü  
	/// C���� BWAPI::Broodwar �� �ش��մϴ�
	public static Game Broodwar;

	/// ���� �����α׷�
	/// @see GameCommander			
	private GameCommander gameCommander;

	public void run() {
		mirror.getModule().setEventListener(this);
		mirror.startGame();
	}

	/// ��Ⱑ ���۵� �� ��ȸ������ �߻��ϴ� �̺�Ʈ�� ó���մϴ�
	@Override
	public void onStart() {

		Broodwar = mirror.getGame();
		
		if (Broodwar.isReplay()) {
			return;
		}

		gameCommander = new GameCommander();
		
		// Config ���� ������ ���ŷӰ�, ���� �� ���� Config ���� ��ġ�� �������ִ� ���� ���ŷӱ� ������, 
		// Config �� ���Ϸκ��� �о������ �ʰ�, Config Ŭ������ ���� ����ϵ��� �Ѵ�.
		if(Config.EnableCompleteMapInformation){
			Broodwar.enableFlag(Enum.CompleteMapInformation.getValue());
		}

		if(Config.EnableUserInput){
			Broodwar.enableFlag(Enum.UserInput.getValue());
		}

		Broodwar.setCommandOptimizationLevel(1);

		// Speedups for automated play, sets the number of milliseconds bwapi spends in each frame
		// Fastest: 42 ms/frame.  1�ʿ� 24 frame. �Ϲ������� 1�ʿ� 24frame�� ���� ���Ӽӵ��� �Ѵ�
		// Normal: 67 ms/frame. 1�ʿ� 15 frame
		// As fast as possible : 0 ms/frame. CPU�� �Ҽ��ִ� ���� ���� �ӵ�. 
		Broodwar.setLocalSpeed(Config.SetLocalSpeed);
		// frameskip�� �ø��� ȭ�� ǥ�õ� ������Ʈ ���ϹǷ� �ξ� ������
		Broodwar.setFrameSkip(Config.SetFrameSkip);

		System.out.println("Map analyzing started");
		BWTA.readMap();
		BWTA.analyze();
		BWTA.buildChokeNodes();
		System.out.println("Map analyzing finished");

		gameCommander.onStart();
	}

	///  ��Ⱑ ����� �� ��ȸ������ �߻��ϴ� �̺�Ʈ�� ó���մϴ�
	@Override
	public void onEnd(boolean isWinner) {
		if (isWinner){
			System.out.println("I won the game");
		} else {
			System.out.println("I lost the game");
		}

		gameCommander.onEnd(isWinner);
		
        System.out.println("Match ended");
        System.exit(0);		
	}

	/// ��� ���� �� �� �����Ӹ��� �߻��ϴ� �̺�Ʈ�� ó���մϴ�
	@Override
	public void onFrame() {
		if (Broodwar.isReplay()) {
			return;
		}

		gameCommander.onFrame();
	}

	/// �ؽ�Ʈ�� �Է� �� ���͸� �Ͽ� �ٸ� �÷��̾�鿡�� �ؽ�Ʈ�� �����Ϸ� �� �� �߻��ϴ� �̺�Ʈ�� ó���մϴ�
	@Override
	public void onSendText(String text){
		ParseTextCommand(text);
		
		gameCommander.onSendText(text);

		// Display the text to the game
		Broodwar.sendText(text);
	}

	/// �ٸ� �÷��̾�κ��� �ؽ�Ʈ�� ���޹޾��� �� �߻��ϴ� �̺�Ʈ�� ó���մϴ�
	@Override
	public void onReceiveText(Player player, String text){
		Broodwar.printf(player.getName() + " said \"" + text + "\"");

		gameCommander.onReceiveText(player, text);
	}

	/// �ٸ� �÷��̾ ����� ������ �� �߻��ϴ� �̺�Ʈ�� ó���մϴ�
	@Override
	public void onPlayerLeft(Player player){
		Broodwar.printf(player.getName() + " left the game.");
	}

	/// �ٹ̻��� �߻簡 �����Ǿ��� �� �߻��ϴ� �̺�Ʈ�� ó���մϴ�
	@Override
	public void onNukeDetect(Position target){
		if (target != Position.Unknown)	{
			Broodwar.drawCircleMap(target, 40, Color.Red, true);
			Broodwar.printf("Nuclear Launch Detected at " + target);
		} else {
			Broodwar.printf("Nuclear Launch Detected");
		}
	}

	/// ����(�ǹ�/��������/��������)�� Create �� �� �߻��ϴ� �̺�Ʈ�� ó���մϴ�
	@Override
	public void onUnitCreate(Unit unit){
		if (!Broodwar.isReplay()) {
			gameCommander.onUnitCreate(unit);
		} else {
			// if we are in a replay, then we will print out the build order
			// (just of the buildings, not the units).
			if (unit.getType().isBuilding() && unit.getPlayer().isNeutral() == false) {
				int seconds = Broodwar.getFrameCount() / 24;
				int minutes = seconds / 60;
				seconds %= 60;
				Broodwar.sendText(minutes + ":" + seconds + ": " +  unit.getPlayer().getName() + " creates a " + unit.getType());
			}
		}
	}

	/// ����(�ǹ�/��������/��������)�� Morph �� �� �߻��ϴ� �̺�Ʈ�� ó���մϴ�
	/// Zerg ������ ������ �ǹ� �Ǽ��̳� ��������/�������� ���꿡�� ���� ��κ� Morph ���·� ����˴ϴ�
	@Override
	public void onUnitMorph(Unit unit){
		if (!Broodwar.isReplay()) {
			gameCommander.onUnitMorph(unit);
		} else {
			// if we are in a replay, then we will print out the build order
			// (just of the buildings, not the units).
			if (unit.getType().isBuilding() && unit.getPlayer().isNeutral() == false) {
				int seconds = Broodwar.getFrameCount() / 24;
				int minutes = seconds / 60;
				seconds %= 60;
				Broodwar.sendText(minutes + ":" + seconds + ": " + unit.getPlayer().getName() + " morphs a " + unit.getType());
			}
		}
	}

	///  ����(�ǹ�/��������/��������)�� Destroy �� �� �߻��ϴ� �̺�Ʈ�� ó���մϴ�
	@Override
	public void onUnitDestroy(Unit unit){
		if (!Broodwar.isReplay()) {
			
			// �й� ���� üũ �� GG
			int buildingCount = 0;
			int workerCount = 0;

			for (Unit u : MyBotModule.Broodwar.self().getUnits()) {
				if (u.getType().isBuilding()) {
					buildingCount++;
				}
				else if (u.getType().isWorker()) {
					workerCount++;
				}
			}
			if (buildingCount == 0) {
				Broodwar.sendText("GG");
				Broodwar.leaveGame();
			}
			
			gameCommander.onUnitDestroy(unit);
		}
	}

	/// ����(�ǹ�/��������/��������)�� Show �� �� �߻��ϴ� �̺�Ʈ�� ó���մϴ�
	/// �Ʊ� ������ Create �Ǿ��� �� ��簡, ���� ������ Discover �Ǿ��� �� �߻��մϴ�
	@Override
	public void onUnitShow(Unit unit){
		if (!Broodwar.isReplay()) {
			gameCommander.onUnitShow(unit);
		}
	}

	/// ����(�ǹ�/��������/��������)�� Hide �� �� �߻��ϴ� �̺�Ʈ�� ó���մϴ�
	/// ���̴� ������ Hide �� �� �߻��մϴ�
	@Override
	public void onUnitHide(Unit unit){
		if (!Broodwar.isReplay()) {
			gameCommander.onUnitHide(unit);
		}
	}

	/// ����(�ǹ�/��������/��������)�� �Ҽ� �÷��̾ �ٲ� �� �߻��ϴ� �̺�Ʈ�� ó���մϴ�
	/// Gas Geyser�� � �÷��̾ Refinery �ǹ��� �Ǽ����� ��, Refinery �ǹ��� �ı��Ǿ��� ��, Protoss ���� Dark Archon �� Mind Control �� ���� �Ҽ� �÷��̾ �ٲ� �� �߻��մϴ�
	@Override
	public void onUnitRenegade(Unit unit){
		if (!Broodwar.isReplay()) {
			gameCommander.onUnitRenegade(unit);
		}
	}

	/// ����(�ǹ�/��������/��������)�� Discover �� �� �߻��ϴ� �̺�Ʈ�� ó���մϴ�
	/// �Ʊ� ������ Create �Ǿ��� �� ��簡, ���� ������ Discover �Ǿ��� �� �߻��մϴ�
	@Override
	public void onUnitDiscover(Unit unit){
		if (!Broodwar.isReplay()) {
			gameCommander.onUnitDiscover(unit);
		}
	}

	/// ����(�ǹ�/��������/��������)�� Evade �� �� �߻��ϴ� �̺�Ʈ�� ó���մϴ�
	/// ������ Destroy �� �� �߻��մϴ�
	@Override
	public void onUnitEvade(Unit unit){
		if (!Broodwar.isReplay()) {
			gameCommander.onUnitEvade(unit);
		}
	}

	/// ����(�ǹ�/��������/��������)�� �ϴ� �� (�ǹ� �Ǽ�, ���׷��̵�, �������� �Ʒ� ��)�� ������ �� �߻��ϴ� �̺�Ʈ�� ó���մϴ�
	@Override
	public void onUnitComplete(Unit unit){
		if (!Broodwar.isReplay()) {
			gameCommander.onUnitComplete(unit);
		}
	}

	/// ������ ������ �� �߻��ϴ� �̺�Ʈ�� ó���մϴ�
	@Override
	public void onSaveGame(String gameName){
		Broodwar.printf("The game was saved to \"" + gameName + "\".");
	}


	/// ����ڰ� �Է��� text �� parse �ؼ� ó���մϴ�
	public void ParseTextCommand(String commandString)
	{
		// Make sure to use %s and pass the text as a parameter,
		// otherwise you may run into problems when you use the %(percent) character!
		Player self = Broodwar.self();

		if ("/afap".equals(commandString)) {
			Broodwar.setLocalSpeed(0);
			Broodwar.setFrameSkip(0);
		} else if ("/fast".equals(commandString)) {
			Broodwar.setLocalSpeed(24);
			Broodwar.setFrameSkip(0);
		} else if ("/slow".equals(commandString)) {
			Broodwar.setLocalSpeed(42);
			Broodwar.setFrameSkip(0);
		} else if ("/endthegame".equals(commandString)) {
			// Not needed if using setGUI(false).
			Broodwar.setGUI(false);
		}
	}
	
}