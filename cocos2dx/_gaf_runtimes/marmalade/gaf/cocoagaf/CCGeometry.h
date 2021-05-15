#pragma once 

#ifndef __GAF_CC_GEOMETRY_H__
#define __GAF_CC_GEOMETRY_H__

#define CCPointMake(x, y) CCPoint((float)(x), (float)(y))
#define CCSizeMake(width, height) CCSize((float)(width), (float)(height))
#define CCRectMake(x, y, width, height) CCRect((float)(x), (float)(y), (float)(width), (float)(height))

namespace GAF
{

class CCPoint
{
public:
	CCPoint();
	CCPoint(float _x, float _y);
	float x, y;
};
	
class CCSize
{
public:
	CCSize();
	CCSize(float w, float h);
	float width, height;
};
	
class CCRect
{
public:
	CCRect();
    CCRect(float x, float y, float width, float height);
    CCPoint origin;
    CCSize  size;
};
	

}

#endif // __GAF_CC_GEOMETRY_H__