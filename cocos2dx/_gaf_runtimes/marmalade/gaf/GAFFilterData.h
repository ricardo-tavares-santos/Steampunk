#pragma once

#ifndef __GAF_FILTER_DATA__
#define __GAF_FILTER_DATA__

#include "cocoagaf/CCObject.h"
#include "cocoagaf/CCGeometry.h"

using namespace GAF;

namespace GAF
{

class GAFFilterData : public CCObject
{
	
};

class GAFBlurFilterData : public GAFFilterData
{
public:
	int blurX;
	int blurY;
	
	static GAFBlurFilterData * create(int _blurX, int _blurY);
	
	inline CCSize blurSize() const
	{
		return CCSizeMake(blurX, blurY);
	}
protected:
	GAFBlurFilterData();
};
	
	
} 

#endif // __GAF_FILTER_DATA__
