#pragma once

#ifndef __GAF_ACTION_OBJECT__
#define __GAF_ACTION_OBJECT__

#include <string>
#include "cocoagaf/CCObject.h"
#include "cocoagaf/CCGeometry.h"

namespace GAF
{
	
class CCDictionary;

class GAFActionObject : public CCObject
{
public:
	std::string name; // TODO : think what is it better than CCString
	CCPoint     pivotPoint;
	CCRect      bounds;
	
	static GAFActionObject * create(CCDictionary * aDictionary);
	bool initWithDictionary(CCDictionary * aDictionary);
protected:
	GAFActionObject();

}; // GAFInteractionObject

} // namespace GAF

#endif // __GAF_ACTION_OBJECT__
