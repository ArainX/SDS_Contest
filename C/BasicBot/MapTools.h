#pragma once

#include "Common.h"

namespace MyBot
{
	class DistanceMap
	{
		int rows, cols, startRow, startCol;
		std::vector<int> dist;
		std::vector<char> moveTo;
		std::vector<BWAPI::TilePosition> sorted;

		int getIndex(const int row, const int col) const
		{
			return row * cols + col;
		}

		int getIndex(const BWAPI::Position & p) const
		{
			return getIndex(p.y / 32, p.x / 32);
		}

	public:

		DistanceMap()
			: dist(std::vector<int>(BWAPI::Broodwar->mapWidth() * BWAPI::Broodwar->mapHeight(), -1))
			, moveTo(std::vector<char>(BWAPI::Broodwar->mapWidth() * BWAPI::Broodwar->mapHeight(), 'X'))
			, rows(BWAPI::Broodwar->mapHeight()), cols(BWAPI::Broodwar->mapWidth()), startRow(-1), startCol(-1)
		{
			//BWAPI::Broodwar->printf("New Distance Map With Dimensions (%d, %d)", rows, cols);
		}

		int & operator [] (const int index)						{ return dist[index]; }
		int & operator [] (const BWAPI::Position & pos)			{ return dist[getIndex(pos.y / 32, pos.x / 32)]; }
		void setMoveTo(const int index, const char val)			{ moveTo[index] = val; }
		void setDistance(const int index, const int val)		{ dist[index] = val; }
		void setStartPosition(const int sr, const int sc)		{ startRow = sr; startCol = sc; }

		/// reset the distance map
		void reset(const int & rows, const int & cols)
		{
			this->rows = rows;
			this->cols = cols;
			dist = std::vector<int>(rows * cols, -1);
			sorted.clear();
			moveTo = std::vector<char>(rows * cols, 'X');
			startRow = -1;
			startCol = -1;
		}

		const std::vector<BWAPI::TilePosition> & getSortedTiles() const
		{
			return sorted;
		}

		/// reset the distance map
		void reset()
		{
			std::fill(dist.begin(), dist.end(), -1);
			std::fill(moveTo.begin(), moveTo.end(), 'X');
			sorted.clear();
			startRow = -1;
			startCol = -1;
		}

		bool isConnected(const BWAPI::Position p) const
		{
			return dist[getIndex(p)] != -1;
		}

		void addSorted(const BWAPI::TilePosition & tp)
		{
			sorted.push_back(tp);
		}

		// given a position, get the position we should move to to minimize distance
		BWAPI::Position getMoveTo(const BWAPI::Position p, const int lookAhead = 1) const
		{
			// the initial row an column
			int row = p.y / 32;
			int col = p.x / 32;

			// for each lookahead
			for (int i = 0; i<lookAhead; ++i)
			{
				// get the index
				int index = getIndex(row, col);

				// adjust the row and column accordingly
				if (moveTo[index] == 'L')
				{
					col -= 1;
				}
				else if (moveTo[index] == 'R')
				{
					col += 1;
				}
				else if (moveTo[index] == 'U')
				{
					row -= 1;
				}
				else
				{
					row += 1;
				}
			}

			// return the position
			return BWAPI::Position(col * 32 + 16, row * 32 + 16);
		}
	};

	/// ������ �ٵ���ó�� Cell ��� ������ ���ؼ� ������ �ϳ��� Cell
	class GridCell
	{
	public:		
		int             timeLastVisited;			///< ���� �������� �湮�ߴ� �ð��� �������� -> Scout �� Ȱ��		
		int             timeLastOpponentSeen;		///< ���� �������� ���� �߰��ߴ� �ð��� �������� -> �� �ǵ� �ľ�, �� �δ� �ľ�, ���� ������ Ȱ��
		BWAPI::Unitset  ourUnits;
		BWAPI::Unitset  oppUnits;
		BWAPI::Position center;

		GridCell()
			: timeLastVisited(0)
			, timeLastOpponentSeen(0)
		{
		}
	};

	/// ������ �ٵ���ó�� Cell ��� ������, �� frame ���� �� Cell �� timeLastVisited �ð�����, timeLastOpponentSeen �ð�����, ourUnits �� oppUnits ����� ������Ʈ �մϴ�
	class MapGrid
	{
		MapGrid();
		MapGrid(int mapWidth, int mapHeight, int cellSize);

		int							cellSize;
		int							mapWidth, mapHeight;
		int							rows, cols;
		int							lastUpdated;

		std::vector< GridCell >		cells;

		void						calculateCellCenters();

		void						clearGrid();
		BWAPI::Position				getCellCenter(int x, int y);

	public:
		/// static singleton ��ü�� �����մϴ�
		static MapGrid &	Instance();

		/// �� Cell �� timeLastVisited �ð�����, timeLastOpponentSeen �ð�����, ourUnits �� oppUnits ��� ���� ������Ʈ �մϴ�
		void				update();

		/// �ش� position ��ó�� �ִ� �Ʊ� Ȥ�� ���� ���ֵ��� ����� UnitSet �� �����մϴ�
		/// BWAPI::Broodwar->self()->getUnitsOnTile, getUnitsInRectangle, getUnitsInRadius, getClosestUnit �Լ��� ���������� ���ӻ��� �ٸ��ϴ�
		void				getUnitsNear(BWAPI::Unitset & units, BWAPI::Position center, int radius, bool ourUnits, bool oppUnits);

		BWAPI::Position		getLeastExplored();

		GridCell &			getCellByIndex(int r, int c)		{ return cells[r*cols + c]; }
		GridCell &			getCell(BWAPI::Position pos)		{ return getCellByIndex(pos.y / cellSize, pos.x / cellSize); }
		GridCell &			getCell(BWAPI::Unit unit)			{ return getCell(unit->getPosition()); }

		int					getCellSize();
		int					getMapWidth();
		int					getMapHeight();
		int					getRows();
		int					getCols();
	};



	/// provides useful tools for analyzing the starcraft map
	/// calculates connectivity and distances using flood fills
	class MapTools
	{
    
		std::map<BWAPI::Position, DistanceMap>       _allMaps;		///< a cache of already computed distance maps
		std::vector<bool>           _map;							///< the map stored at TilePosition resolution, values are 0/1 for walkable or not walkable
		std::vector<bool>           _units;							///< map that stores whether a unit is on this position
		std::vector<int>            _fringe;						///< the fringe vector which is used as a sort of 'open list'
		int                         _rows;
		int                         _cols;

		MapTools();

		int                     getIndex(int row,int col);			///< return the index of the 1D array from (row,col)
		bool                    unexplored(DistanceMap & dmap,const int index) const;
		void                    reset();							///< resets the distance and fringe vectors, call before each search    
		void                    setBWAPIMapData();					///< reads in the map data from bwapi and stores it in our map format
		void                    resetFringe();
		void                    computeDistance(DistanceMap & dmap,const BWAPI::Position p); ///< computes walk distance from Position P to all other points on the map
		BWAPI::TilePosition     getTilePosition(int index);		
		int                     getGroundDistance(BWAPI::Position from, BWAPI::Position to); ///< from ���� to ���� ���������� �̵��� ����� �Ÿ� (walk distance)


	public:
		/// static singleton ��ü�� �����մϴ�
		static MapTools &       Instance();

		/// ������ Parsing �ؼ� ���Ϸ� �����صд�
		/// ������� �ʴ� API
		void                    parseMap();
		void                    search(DistanceMap & dmap,const int sR,const int sC);
		void                    fill(const int index,const int region);

		/// Position ���� ����� ������� Ÿ���� ����� ��ȯ�Ѵ�
		const std::vector<BWAPI::TilePosition> & getClosestTilesTo(BWAPI::Position pos);
	};

}