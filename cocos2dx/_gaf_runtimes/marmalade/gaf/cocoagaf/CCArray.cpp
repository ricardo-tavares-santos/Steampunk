#include "CCArray.h"

namespace GAF
{

CCArray* CCArray::create()
{
    CCArray* res = new CCArray();
    if (res)
    {
        return (CCArray*)res->autorelease();
    }
    else
    {
        CC_SAFE_DELETE(res);
		return NULL;
    }
}
	
CCArray* CCArray::createWithCapacity(int size)
{
	CCArray* res = new CCArray();
	res->_vect.resize(size);
    if (res)
    {
        return (CCArray*)res->autorelease();
    }
    else
    {
        CC_SAFE_DELETE(res);
		return NULL;
    }
}
	
void CCArray::removeAllObjects()
{
	for (unsigned int i = 0; i < _vect.size(); ++i)
	{
		CC_SAFE_RELEASE(_vect[i]);
	}
	_vect.clear();
}

} // namespace GAF