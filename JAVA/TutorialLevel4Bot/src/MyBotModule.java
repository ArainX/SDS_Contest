import bwapi.Color;
import bwapi.DefaultBWListener;
import bwapi.Game;
import bwapi.Mirror;
import bwapi.Player;
import bwapi.Position;
import bwapi.Unit;
import bwapi.Flag.Enum;
import bwta.BWTA;

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

	@Override
	public void onStart() {
		Broodwar = mirror.getGame();
		
		if (Broodwar.isReplay()) {
			return;
		}

		gameCommander = new GameCommander();
		
		// ��ü ���� �� ����� �̺�Ʈ�� �� �ľ��ϴ� ���
		//game.enableFlag(Enum.CompleteMapInformation.getValue());

		// Ű����/���콺�� ���� �÷��̸� ������ �� �ִ� ���
		Broodwar.enableFlag(Enum.UserInput.getValue());

		// ������ ���� ����� �ϳ��� ó���ؼ� CPU �δ��� �ٿ���
		Broodwar.setCommandOptimizationLevel(1);

		// Speedups for automated play, sets the number of milliseconds bwapi spends in each frame
		// Fastest: 42 ms/frame.  1�ʿ� 24 frame. �Ϲ������� 1�ʿ� 24frame�� ���� ���Ӽӵ��� �Ѵ�
		// Normal: 67 ms/frame. 1�ʿ� 15 frame
		// As fast as possible : 0 ms/frame. CPU�� �Ҽ��ִ� ���� ���� �ӵ�. 
		Broodwar.setLocalSpeed(15);
		// frameskip�� �ø��� ȭ�� ǥ�õ� ������Ʈ ���ϹǷ� �ξ� ������
		Broodwar.setFrameSkip(0);

		System.out.println("Map analyzing started");
		BWTA.readMap();
		BWTA.analyze();
		BWTA.buildChokeNodes();
		System.out.println("Map analyzing finished");		
	
		System.out.println("Hello Starcraft : Broodwar command prompt");

		Broodwar.printf("Hello Starcraft : Broodwar game screen");
		
		gameCommander.onStart();
	}

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

	@Override
	public void onFrame() {
		// ���÷��� ����� ��� �ƹ��͵� ���� ����
		if (Broodwar.isReplay()) {
			return;
		}

		gameCommander.onFrame();
	}

	@Override
	public void onSendText(String text){
		// Display the text to the game
		Broodwar.sendText(text);
		
		Broodwar.printf(text);
	}

	@Override
	public void onReceiveText(Player player, String text){
		Broodwar.printf(player.getName() + " said \"" + text + "\"");
	}

	@Override
	public void onPlayerLeft(Player player){
		Broodwar.printf(player.getName() + " left the game.");
	}

	@Override
	public void onNukeDetect(Position target){
		if (target != Position.Unknown)	{
			Broodwar.drawCircleMap(target, 40, Color.Red, true);
			Broodwar.printf("Nuclear Launch Detected at " + target);
		} else {
			Broodwar.printf("Nuclear Launch Detected");
		}
	}

	@Override
	public void onUnitCreate(Unit unit){
		if (unit.getPlayer().isNeutral() == false) {
			Broodwar.printf(unit.getType() + " " + unit.getID() + " created at " + unit.getTilePosition().getX() + ", " + unit.getTilePosition().getY());
		}	
		
		if (!Broodwar.isReplay()) {
			gameCommander.onUnitCreate(unit);
		}
	}

	@Override
	public void onUnitMorph(Unit unit){
		if (unit.getPlayer().isNeutral() == false) {
			Broodwar.printf(unit.getType() + " " + unit.getID() + " morphed at " + unit.getTilePosition().getX() + ", " + unit.getTilePosition().getY());
		}		
		
		if (!Broodwar.isReplay()) {
			gameCommander.onUnitMorph(unit);
		}
	}

	@Override
	public void onUnitDestroy(Unit unit){
		if (unit.getPlayer().isNeutral() == false) {
			Broodwar.printf(unit.getType() + " " + unit.getID() + " destroyed at " + unit.getTilePosition().getX() + ", " + unit.getTilePosition().getY());
		}		
		
		if (!Broodwar.isReplay()) {
			gameCommander.onUnitDestroy(unit);
		}	
	}

	@Override
	public void onUnitShow(Unit unit){
		if (unit.getPlayer().isNeutral() == false) {
			Broodwar.printf(unit.getType() + " " + unit.getID() + " showed at " + unit.getTilePosition().getX() + ", " + unit.getTilePosition().getY());
		}		
		
		if (!Broodwar.isReplay()) {
			gameCommander.onUnitShow(unit);
		}	
	}

	@Override
	public void onUnitHide(Unit unit){
		if (unit.getPlayer().isNeutral() == false) {
			Broodwar.printf(unit.getType() + " " + unit.getID() + " hid at " + unit.getTilePosition().getX() + ", " + unit.getTilePosition().getY());
		}		
		
		if (!Broodwar.isReplay()) {
			gameCommander.onUnitHide(unit);
		}	
	}

	@Override
	public void onUnitRenegade(Unit unit){
		if (unit.getPlayer().isNeutral() == false) {
			Broodwar.printf(unit.getType() + " " + unit.getID() + " renegaded at " + unit.getTilePosition().getX() + ", " + unit.getTilePosition().getY());
		}		
		
		if (!Broodwar.isReplay()) {
			gameCommander.onUnitRenegade(unit);
		}	
	}

	@Override
	public void onUnitDiscover(Unit unit){
		if (unit.getPlayer().isNeutral() == false) {
			Broodwar.printf(unit.getType() + " " + unit.getID() + " discovered at " + unit.getTilePosition().getX() + ", " + unit.getTilePosition().getY());
		}		
		
		if (!Broodwar.isReplay()) {
			gameCommander.onUnitDiscover(unit);
		}	
	}

	@Override
	public void onUnitEvade(Unit unit){
		if (unit.getPlayer().isNeutral() == false) {
			Broodwar.printf(unit.getType() + " " + unit.getID() + " evaded at " + unit.getTilePosition().getX() + ", " + unit.getTilePosition().getY());
		}		
		
		if (!Broodwar.isReplay()) {
			gameCommander.onUnitEvade(unit);
		}	
	}

	@Override
	public void onUnitComplete(Unit unit){
		if (unit.getPlayer().isNeutral() == false) {
			Broodwar.printf(unit.getType() + " " + unit.getID() + " completed at " + unit.getTilePosition().getX() + ", " + unit.getTilePosition().getY());
		}		
		
		if (!Broodwar.isReplay()) {
			gameCommander.onUnitComplete(unit);
		}	
	}

	@Override
	public void onSaveGame(String gameName){
		Broodwar.printf("The game was saved to \"" + gameName + "\".");
	}
}