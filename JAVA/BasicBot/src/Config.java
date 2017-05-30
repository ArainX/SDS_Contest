import java.util.Set;

import bwapi.Color;
import bwapi.UnitType;

/// �� ���α׷� ����
public class Config {

	
	
	/// �� �̸�
	public static final String BotName = "NoNameBot";
	/// �� ������ �̸�
	public static final String BotAuthors = "NoName";
	
	
	
	/// �α� ���� �̸�
	public static final String LogFilename = "log.txt";
	/// �б� ���� ���
	public static final String ReadDirectory = "bwapi-data\\AI\\MyBot\\read\\";
	/// ���� ���� ���
	public static final String WriteDirectory = "bwapi-data\\AI\\MyBot\\write\\";		

	
	

	/// ���ÿ��� ������ ������ �� ���ӽ��ǵ� (�ڵ� ���� �� �������� ������ ������ ���� ���� ������ �����)
	/// Speedups for automated play, sets the number of milliseconds bwapi spends in each frame
	/// Fastest: 42 ms/frame.  1�ʿ� 24 frame. �Ϲ������� 1�ʿ� 24frame�� ���� ���Ӽӵ��� �մϴ�
	/// Normal: 67 ms/frame. 1�ʿ� 15 frame
	/// As fast as possible : 0 ms/frame. CPU�� �Ҽ��ִ� ���� ���� �ӵ�.
	public static final int SetLocalSpeed = 10;
	
	/// ���ÿ��� ������ ������ �� FrameSkip (�ڵ� ���� �� �������� ������ ������ ���� ���� ������ �����)
	/// frameskip�� �ø��� ȭ�� ǥ�õ� ������Ʈ ���ϹǷ� �ξ� �����ϴ�
    public static final int SetFrameSkip = 0;
    
    /// ���ÿ��� ������ ������ �� ����� Ű����/���콺 �Է� ��� ���� (�ڵ� ���� �� �������� ������ ������ ���� ���� ������ �����)	
    public static final boolean EnableUserInput = true;
    
    /// ���ÿ��� ������ ������ �� ��ü ������ �� ���̰� �� ������ ���� (�ڵ� ���� �� �������� ������ ������ ���� ���� ������ �����)    
	public static final boolean EnableCompleteMapInformation = false;

	
	/// MapGrid ���� �� �� GridCell �� size
	public static final int MAP_GRID_SIZE = 32;
	
	/// StarCraft �� BWAPI ���� 1 Tile = 32 * 32 Point (Pixel) �Դϴ�
	/// Position �� Point (Pixel) �����̰�, TilePosition �� Tile �����Դϴ� 
	public static final int TILE_SIZE = 32;

	/// ������ Refinery ���� ������ �ϲ� �ִ� ����
	public static final int WorkersPerRefinery = 3;
	/// �ǹ��� �ǹ��� ��� �ּ����� ���� - �Ϲ����� �ǹ��� ���
	public static final int BuildingSpacing = 2;
	/// �ǹ��� �ǹ��� ��� �ּ����� ���� - ResourceDepot �ǹ��� ��� (Nexus, Hatchery, Command Center)
	public static final int BuildingResourceDepotSpacing = 0;
	/// �ǹ��� �ǹ��� ��� �ּ����� ���� - Protoss_Pylon �ǹ��� ��� - ���� �ʱ⿡
	public static final int BuildingPylonEarlyStageSpacing = 4;
	/// �ǹ��� �ǹ��� ��� �ּ����� ���� - Protoss_Pylon �ǹ��� ��� - ���� �ʱ� ���Ŀ�
	public static final int BuildingPylonSpacing = 2;
	/// �ǹ��� �ǹ��� ��� �ּ����� ���� - Terran_Supply_Depot �ǹ��� ���
	public static final int BuildingSupplyDepotSpacing = 0;
	/// �ǹ��� �ǹ��� ��� �ּ����� ���� - ��� �ǹ��� ��� (����ĳ��. ��ū�ݷδ�. �������ݷδ�. �ͷ�. ��Ŀ)
	public static final int BuildingDefenseTowerSpacing = 0; 
	
	
	

	/// ȭ�� ǥ�� ���� - ���� ����
	public static final boolean DrawGameInfo = true;
	
	/// ȭ�� ǥ�� ���� - �̳׶�, ����
	public static final boolean DrawResourceInfo = true;
	/// ȭ�� ǥ�� ���� - ����
	public static final boolean DrawBWTAInfo = true;
	/// ȭ�� ǥ�� ���� - �ٵ���
	public static final boolean DrawMapGrid = true;

	/// ȭ�� ǥ�� ���� - ���� HitPoint
	public static final boolean DrawUnitHealthBars = true;
	/// ȭ�� ǥ�� ���� - ���� ���
	public static final boolean DrawEnemyUnitInfo = true;
	/// ȭ�� ǥ�� ���� - ���� ~ Target �� ����
	public static final boolean DrawUnitTargetInfo = true;

	/// ȭ�� ǥ�� ���� - ���� ť
	public static final boolean DrawProductionInfo = true;

	/// ȭ�� ǥ�� ���� - �ǹ� Construction ��Ȳ
	public static final boolean DrawBuildingInfo = true;
	/// ȭ�� ǥ�� ���� - �ǹ� ConstructionPlace ���� ��Ȳ
	public static final boolean DrawReservedBuildingTiles = true;
	
	/// ȭ�� ǥ�� ���� - ���� ����
	public static final boolean DrawScoutInfo = true;
	/// ȭ�� ǥ�� ���� - �ϲ� ���
	public static final boolean DrawWorkerInfo = true;
	
	/// ȭ�� ǥ�� ���� - ���콺 Ŀ��	
	public static final boolean DrawMouseCursorInfo = true;

	public static final Color ColorLineTarget = Color.White;
	public static final Color ColorLineMineral = Color.Cyan;
	public static final Color ColorUnitNearEnemy = Color.Red;
	public static final Color ColorUnitNotNearEnemy = Color.Green;
	
	
}