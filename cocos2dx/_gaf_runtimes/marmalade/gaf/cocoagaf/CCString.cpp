#include "CCString.h"

namespace GAF
{

CCString * CCString::create(const char * str)
{
	CCString* pRet = new CCString(str);
    pRet->autorelease();
    return pRet;
}

}