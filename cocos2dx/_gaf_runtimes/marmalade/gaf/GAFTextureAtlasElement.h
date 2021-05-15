#pragma once

#ifndef __GAF_TEXTURE_ATLAS_ELEMENT__
#define __GAF_TEXTURE_ATLAS_ELEMENT__

#include <string>

#include "cocoagaf/CCObject.h"
#include "cocoagaf/CCGeometry.h"

namespace GAF
{
	class CCDictionary;
}

using namespace GAF;

class GAFTextureAtlasElement : public CCObject
{
public:
	std::string name;	
	CCPoint     pivotPoint;
	CCRect      bounds;
	int         atlasIdx;

	static GAFTextureAtlasElement * create(CCDictionary * aDictionary);
	bool initWithDictionary(CCDictionary * aDictionary);
private:
	GAFTextureAtlasElement();
}; // GAFTextureAtlasElement

#endif // __GAF_TEXTURE_ATLAS_ELEMENT__
