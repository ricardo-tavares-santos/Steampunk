#include "CCDictionary.h"

namespace GAF
{

CCDictionary* CCDictionary::create()
{
	CCDictionary* pRet = new CCDictionary();
	if (pRet != NULL)
	{
		pRet->autorelease();
	}
	return pRet;
}
	
void CCDictionary::setObject(CCObject * object, const char * key)
{
	object->retain();
	(*this)[key] = object;
}

void CCDictionary::removeAllObjects()
{
	CCDICT_FOREACH(this, pElement)
    {
		CC_SAFE_RELEASE(pElement->second);
	}
	this->clear();
}

	
}

