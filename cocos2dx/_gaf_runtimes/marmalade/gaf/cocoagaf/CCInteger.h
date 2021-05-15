#pragma once

#ifndef __GAF_COOCA_CCINTEGER__
#define __GAF_COOCA_CCINTEGER__

#include "CCObject.h"

namespace GAF
{
	
class CCInteger : public CCObject
{
public:
	inline int getValue() const
	{
		return _value;
	}
private:
    int _value;
};

}


#endif // __GAF_COOCA_CCINTEGER__