#pragma once

#define _USE_MATH_DEFINES
#include <cmath>
#include <math.h>
#include <cstdio>
#include <cstdlib>

#include <stdarg.h>
#include <stdexcept>
#include <string>
#include <iostream>
#include <fstream>
#include <sstream>
#include <algorithm>
#include <vector>
#include <deque>
#include <list>
#include <set>
#include <map>
#include <array>
#include <ctime>
#include <iomanip>

#include <winsock2.h>
#include <windows.h>

#include <BWAPI.h>
#include <BWTA.h>

#include "Config.h"
#include "CommandUtil.h"

namespace MyBot
{

	struct double2
	{
		double x, y;

		double2() {}
		double2(double x, double y) : x(x), y(y) {}
		double2(const BWAPI::Position & p) : x(p.x), y(p.y) {}

		operator BWAPI::Position()				const { return BWAPI::Position(static_cast<int>(x), static_cast<int>(y)); }

		double2 operator + (const double2 & v)	const { return double2(x + v.x, y + v.y); }
		double2 operator - (const double2 & v)	const { return double2(x - v.x, y - v.y); }
		double2 operator * (double s)			const { return double2(x*s, y*s); }
		double2 operator / (double s)			const { return double2(x / s, y / s); }

		double dot(const double2 & v)			const { return x*v.x + y*v.y; }
		double lenSq()							const { return x*x + y*y; }
		double len()							const { return sqrt(lenSq()); }
		double2 normal()						const { return *this / len(); }

		void normalise() { double s(len()); x /= s; y /= s; }
		void rotate(double angle)
		{
			angle = angle*M_PI / 180.0;
			*this = double2(x * cos(angle) - y * sin(angle), y * cos(angle) + x * sin(angle));
		}
	};

	/// �α� ��ƿ
	namespace Logger
	{
		void appendTextToFile(const std::string & logFile, const std::string & msg);
		void appendTextToFile(const std::string & logFile, const char *fmt, ...);
		void overwriteToFile(const std::string & logFile, const std::string & msg);
	};

	/// ���� ��ƿ
	namespace FileUtil {
		/// ���� ��ƿ - �ؽ�Ʈ ������ �о���δ�
		std::string readFile(const std::string & filename);

		/// ���� ��ƿ - ��� ����� �ؽ�Ʈ ���Ϸκ��� �о���δ�
		void readResults();

		/// ���� ��ƿ - ��� ����� �ؽ�Ʈ ���Ͽ� �����Ѵ�
		void writeResults();
	}	

}