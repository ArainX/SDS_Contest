import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

import bwapi.Color;
import bwapi.Position;
import bwapi.TilePosition;
import bwapi.Unit;
import bwta.BWTA;
import bwta.BaseLocation;
import bwta.Region;

/// ���� �ʹݿ� �ϲ� ���� �߿��� ���� ������ �ϳ� �����ϰ�, ���� ������ �̵����� ������ �����ϴ� class
/// ������ BaseLocation ��ġ�� �˾Ƴ��� �ͱ����� ���ߵǾ��ֽ��ϴ�
public class ScoutManager {

	private Unit currentScoutUnit;
	private int currentScoutStatus;
	
	public enum ScoutStatus {
		NoScout,						///< ���� ������ �������� ����
		MovingToAnotherBaseLocation,	///< ������ BaseLocation �� �̹߰ߵ� ���¿��� ���� ������ �̵���Ű�� �ִ� ����
		MoveAroundEnemyBaseLocation   	///< ������ BaseLocation �� �߰ߵ� ���¿��� ���� ������ �̵���Ű�� �ִ� ����
	};
	
	private BaseLocation currentScoutTargetBaseLocation = null;
	private Vector<Position> enemyBaseRegionVertices = new Vector<Position>();
	private int currentScoutFreeToVertexIndex = -1;
	private Position currentScoutTargetPosition = Position.None;

	private CommandUtil commandUtil = new CommandUtil();
	
	private static ScoutManager instance = new ScoutManager();
	
	/// static singleton ��ü�� �����մϴ�
	public static ScoutManager Instance() {
		return instance;
	} 

	/// ���� ������ �����ϰ�, ���� ���¸� ������Ʈ�ϰ�, ���� ������ �̵���ŵ�ϴ�
	public void update()
	{
		// 1�ʿ� 4���� �����մϴ�
		if (MyBotModule.Broodwar.getFrameCount() % 6 != 0) return;
		
		// scoutUnit �� �����ϰ�, scoutUnit �� �̵��� ��Ʈ����. 
		// TODO ���� : ���� scoutUnit �� ���ÿ� ����ϰų�, scoutUnit �� ��񿡼� ������ ���� ����Ͽ� ������ ��� �����ϴ� ���, ���Ĺ� ������ ���� ó�� ���� �����غ� �����̴�  
		assignScoutIfNeeded();
		moveScoutUnit();

		// �����, scoutUnit �� �̵��� ���� �߰ߵ� ������ ó���ϴ� ���� InformationManager.update() ���� ������
	}

	/// ���� ������ �ʿ��ϸ� ���� �����մϴ�
	public void assignScoutIfNeeded()
	{
		BaseLocation enemyBaseLocation = InformationManager.Instance().getMainBaseLocation(MyBotModule.Broodwar.enemy());

		if (enemyBaseLocation == null)
		{
			if (currentScoutUnit == null || currentScoutUnit.exists() == false || currentScoutUnit.getHitPoints() <= 0)
			{
				currentScoutUnit = null;
				currentScoutStatus = ScoutStatus.NoScout.ordinal();

				// first building (Pylon / Supply Depot / Spawning Pool) �� �Ǽ� ������ ��, ���� �����̿� �ִ� Worker �� ������������ �����Ѵ�
				Unit firstBuilding = null;

				for (Unit unit : MyBotModule.Broodwar.self().getUnits())
				{
					if (unit.getType().isBuilding() == true && unit.getType().isResourceDepot() == false)
					{
						firstBuilding = unit;
						break;
					}
				}

				if (firstBuilding != null)
				{
					// grab the closest worker to the first building to send to scout
					Unit unit = WorkerManager.Instance().getClosestMineralWorkerTo(firstBuilding.getPosition());

					// if we find a worker (which we should) add it to the scout units
					// ���� ���� �ϲ��� ������, �ƹ��͵� ���� �ʴ´�
					if (unit != null)
					{
						// set unit as scout unit
						currentScoutUnit = unit;
						WorkerManager.Instance().setScoutWorker(currentScoutUnit);

						// �����, �ϲ��� ���� �ӹ��� �����Ϸ���, ������ ���� �ϸ� �ȴ�
						//WorkerManager::Instance().setIdleWorker(currentScoutUnit);
					}
				}
			}
		}
	}


	/// ���� ������ �̵���ŵ�ϴ�
	// ���� MainBaseLocation ��ġ�� �𸣴� ��Ȳ�̸�, StartLocation �鿡 ���� �Ʊ��� MainBaseLocation���� ����� �ͺ��� ������� ����
	// ���� MainBaseLocation ��ġ�� �ƴ� ��Ȳ�̸�, �ش� BaseLocation �� �ִ� Region�� �����ڸ��� ���� ��� �̵��� (���� ������ ����������) 
	public void moveScoutUnit()
	{
		if (currentScoutUnit == null || currentScoutUnit.exists() == false || currentScoutUnit.getHitPoints() <= 0 )
		{
			currentScoutUnit = null;
			currentScoutStatus = ScoutStatus.NoScout.ordinal();
			return;
		}

		BaseLocation enemyBaseLocation = InformationManager.Instance().getMainBaseLocation(InformationManager.Instance().enemyPlayer);
		BaseLocation myBaseLocation = InformationManager.Instance().getMainBaseLocation(MyBotModule.Broodwar.self());

		if (enemyBaseLocation == null)
		{
			// currentScoutTargetBaseLocation �� null �̰ų� ���� ������ currentScoutTargetBaseLocation �� ���������� 
			// �Ʊ� MainBaseLocation ���κ��� ���� ����� ������ BaseLocation �� ���ο� ���� ��� currentScoutTargetBaseLocation ���� ��Ƽ� �̵�
			if (currentScoutTargetBaseLocation == null || currentScoutUnit.getDistance(currentScoutTargetBaseLocation.getPosition()) < 5 * Config.TILE_SIZE) 
			{
				currentScoutStatus = ScoutStatus.MovingToAnotherBaseLocation.ordinal();

				double closestDistance = 1000000000;
				double tempDistance = 0;
				BaseLocation closestBaseLocation = null;
				for (BaseLocation startLocation : BWTA.getStartLocations())
				{
					// if we haven't explored it yet (�湮�߾��� ���� �ٽ� ���� �ʿ� ����)
					if (MyBotModule.Broodwar.isExplored(startLocation.getTilePosition()) == false)
					{
						// GroundDistance �� �������� ���� ����� ������ ����
						tempDistance = (double)(InformationManager.Instance().getMainBaseLocation(MyBotModule.Broodwar.self()).getGroundDistance(startLocation) + 0.5);

						if (tempDistance > 0 && tempDistance < closestDistance) {
							closestBaseLocation = startLocation;
							closestDistance = tempDistance;
						}
					}
				}

				if (closestBaseLocation != null) {
					// assign a scout to go scout it
					commandUtil.move(currentScoutUnit, closestBaseLocation.getPosition());
					currentScoutTargetBaseLocation = closestBaseLocation;
				}
			}
		}
		// if we know where the enemy region is
		else 
		{
			// if scout is exist, move scout into enemy region
			if (currentScoutUnit != null) {
				
				currentScoutTargetBaseLocation = enemyBaseLocation;
				
				if (MyBotModule.Broodwar.isExplored(currentScoutTargetBaseLocation.getTilePosition()) == false) {
					
					currentScoutStatus = ScoutStatus.MovingToAnotherBaseLocation.ordinal();
					currentScoutTargetPosition = currentScoutTargetBaseLocation.getPosition();
					commandUtil.move(currentScoutUnit, currentScoutTargetPosition);
					
				}
				else {
					//currentScoutStatus = ScoutStatus.MoveAroundEnemyBaseLocation.ordinal();
					//currentScoutTargetPosition = getScoutFleePositionFromEnemyRegionVertices();
					//commandUtil.move(currentScoutUnit, currentScoutTargetPosition);					
					
					WorkerManager.Instance().setIdleWorker(currentScoutUnit);
					currentScoutStatus = ScoutStatus.NoScout.ordinal();
					currentScoutTargetPosition = myBaseLocation.getPosition();
				}
			}
		}
	}

	public Position getScoutFleePositionFromEnemyRegionVertices()
	{
		// calculate enemy region vertices if we haven't yet
		if (enemyBaseRegionVertices.isEmpty()) {
			calculateEnemyRegionVertices();
		}

		if (enemyBaseRegionVertices.isEmpty()) {
			return MyBotModule.Broodwar.self().getStartLocation().toPosition();
		}

		// if this is the first flee, we will not have a previous perimeter index
		if (currentScoutFreeToVertexIndex == -1)
		{
			// so return the closest position in the polygon
			int closestPolygonIndex = getClosestVertexIndex(currentScoutUnit);

			if (closestPolygonIndex == -1)
			{
				return MyBotModule.Broodwar.self().getStartLocation().toPosition();
			}
			else
			{
				// set the current index so we know how to iterate if we are still fleeing later
				currentScoutFreeToVertexIndex = closestPolygonIndex;
				return enemyBaseRegionVertices.get(closestPolygonIndex);
			}
		}
		// if we are still fleeing from the previous frame, get the next location if we are close enough
		else
		{
			double distanceFromCurrentVertex = enemyBaseRegionVertices.get(currentScoutFreeToVertexIndex).getDistance(currentScoutUnit.getPosition());

			// keep going to the next vertex in the perimeter until we get to one we're far enough from to issue another move command
			while (distanceFromCurrentVertex < 128)
			{
				currentScoutFreeToVertexIndex = (currentScoutFreeToVertexIndex + 1) % enemyBaseRegionVertices.size();
				distanceFromCurrentVertex = enemyBaseRegionVertices.get(currentScoutFreeToVertexIndex).getDistance(currentScoutUnit.getPosition());
			}

			return enemyBaseRegionVertices.get(currentScoutFreeToVertexIndex);
		}
	}

	// Enemy MainBaseLocation �� �ִ� Region �� �����ڸ���  enemyBaseRegionVertices �� �����Ѵ�
	// Region �� ��� �ǹ��� Eliminate ��Ű�� ���� ���� Ž�� ���� �ۼ��� ������ �� �ִ�
	public void calculateEnemyRegionVertices()
	{
		BaseLocation enemyBaseLocation = InformationManager.Instance().getMainBaseLocation(MyBotModule.Broodwar.enemy());
		if (enemyBaseLocation == null) {
			return;
		}

		Region enemyRegion = enemyBaseLocation.getRegion();
		if (enemyRegion == null) {
			return;
		}
		final Position basePosition = MyBotModule.Broodwar.self().getStartLocation().toPosition();
		final Vector<TilePosition> closestTobase = MapTools.Instance().getClosestTilesTo(basePosition);
		Set<Position> unsortedVertices = new HashSet<Position>();

		// check each tile position
		for (int i = 0; i < closestTobase.size(); ++i)
		{
			final TilePosition tp = closestTobase.get(i);

			if (BWTA.getRegion(tp) != enemyRegion)
			{
				continue;
			}

			// a tile is 'surrounded' if
			// 1) in all 4 directions there's a tile position in the current region
			// 2) in all 4 directions there's a buildable tile
			boolean surrounded = true;
			if (BWTA.getRegion(new TilePosition(tp.getX() + 1, tp.getY())) != enemyRegion || !MyBotModule.Broodwar.isBuildable(new TilePosition(tp.getX() + 1, tp.getY()))
					|| BWTA.getRegion(new TilePosition(tp.getX(), tp.getY() + 1)) != enemyRegion || !MyBotModule.Broodwar.isBuildable(new TilePosition(tp.getX(), tp.getY() + 1))
					|| BWTA.getRegion(new TilePosition(tp.getX() - 1, tp.getY())) != enemyRegion || !MyBotModule.Broodwar.isBuildable(new TilePosition(tp.getX() - 1, tp.getY()))
					|| BWTA.getRegion(new TilePosition(tp.getX(), tp.getY() - 1)) != enemyRegion || !MyBotModule.Broodwar.isBuildable(new TilePosition(tp.getX(), tp.getY() - 1)))
			{
				surrounded = false;
			}

			// push the tiles that aren't surrounded 
			// Region�� �����ڸ� Ÿ�ϵ鸸 �߰��Ѵ�
			if (!surrounded && MyBotModule.Broodwar.isBuildable(tp))
			{
				if (Config.DrawScoutInfo)
				{
					int x1 = tp.getX() * 32 + 2;
					int y1 = tp.getY() * 32 + 2;
					int x2 = (tp.getX() + 1) * 32 - 2;
					int y2 = (tp.getY() + 1) * 32 - 2;
					MyBotModule.Broodwar.drawTextMap(x1 + 3, y1 + 2, "" + BWTA.getGroundDistance(tp, basePosition.toTilePosition()));
					MyBotModule.Broodwar.drawBoxMap(x1, y1, x2, y2, Color.Green, false);
				}

				unsortedVertices.add(new Position(tp.toPosition().getX() + 16, tp.toPosition().getY() + 16));
			}
		}

		Vector<Position> sortedVertices = new Vector<Position>();
		Position current = unsortedVertices.iterator().next();
		enemyBaseRegionVertices.add(current);
		unsortedVertices.remove(current);

		// while we still have unsorted vertices left, find the closest one remaining to current
		while (!unsortedVertices.isEmpty())
		{
			double bestDist = 1000000;
			Position bestPos = null;

			for (final Position pos : unsortedVertices)
			{
				double dist = pos.getDistance(current);

				if (dist < bestDist)
				{
					bestDist = dist;
					bestPos = pos;
				}
			}

			current = bestPos;
			sortedVertices.add(bestPos);
			unsortedVertices.remove(bestPos);
		}

		// let's close loops on a threshold, eliminating death grooves
		int distanceThreshold = 100;

		while (true)
		{
			// find the largest index difference whose distance is less than the threshold
			int maxFarthest = 0;
			int maxFarthestStart = 0;
			int maxFarthestEnd = 0;

			// for each starting vertex
			for (int i = 0; i < (int)sortedVertices.size(); ++i)
			{
				int farthest = 0;
				int farthestIndex = 0;

				// only test half way around because we'll find the other one on the way back
				for (int j= 1; j < sortedVertices.size() / 2; ++j)
				{
					int jindex = (i + j) % sortedVertices.size();

					if (sortedVertices.get(i).getDistance(sortedVertices.get(jindex)) < distanceThreshold)
					{
						farthest = j;
						farthestIndex = jindex;
					}
				}

				if (farthest > maxFarthest)
				{
					maxFarthest = farthest;
					maxFarthestStart = i;
					maxFarthestEnd = farthestIndex;
				}
			}

			// stop when we have no long chains within the threshold
			if (maxFarthest < 4)
			{
				break;
			}

			double dist = sortedVertices.get(maxFarthestStart).getDistance(sortedVertices.get(maxFarthestEnd));

			Vector<Position> temp = new Vector<Position>();

			for (int s = maxFarthestEnd; s != maxFarthestStart; s = (s + 1) % sortedVertices.size())
			{
				
				temp.add(sortedVertices.get(s));
			}

			sortedVertices = temp;
		}

		enemyBaseRegionVertices = sortedVertices;
	}

	public int getClosestVertexIndex(Unit unit)
	{
		int closestIndex = -1;
		double closestDistance = 10000000;

		for (int i = 0; i < enemyBaseRegionVertices.size(); ++i)
		{
			double dist = unit.getDistance(enemyBaseRegionVertices.get(i));
			if (dist < closestDistance)
			{
				closestDistance = dist;
				closestIndex = i;
			}
		}

		return closestIndex;
	}
	
	/// ���� ������ �����մϴ�
	public Unit getScoutUnit()
	{
		return currentScoutUnit;
	}

	// ���� ���¸� �����մϴ�
	public int getScoutStatus()
	{
		return currentScoutStatus;
	}

	/// ���� ������ �̵� ��ǥ BaseLocation �� �����մϴ�
	public BaseLocation getScoutTargetBaseLocation()
	{
		return currentScoutTargetBaseLocation;
	}

	/// ������ Main Base Location �� �ִ� Region �� ��輱�� �ش��ϴ� Vertex ���� ����� �����մϴ�
	public Vector<Position> getEnemyRegionVertices()
	{
		return enemyBaseRegionVertices;
	}
}