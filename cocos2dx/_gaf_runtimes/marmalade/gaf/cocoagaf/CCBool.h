#pragma once

#ifndef __GAF_COOCA_CCBOOL__
#define __GAF_COOCA_CCBOOL__

#include "CCObject.h"

namespace GAF
{
	
class CCBool : public CCObject
{
public:
	inline bool getValue() const
	{
		return _value;
	}
	static CCBool * create(bool v);
protected:
	CCBool(bool v)
	:
	_value(v)
	{
		
	}
private:
    bool _value;
};

}


#endif // __GAF_COOCA_CCBOOL__