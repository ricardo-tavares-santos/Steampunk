#pragma once

#ifndef __GAF_COCOA_CCSTRING___
#define __GAF_COCOA_CCSTRING___

#include "CCObject.h"
#include <string>

namespace GAF
{
	class CCString : public CCObject
	{
	public:
		const char * getCString() const
		{
			return _str.c_str();
		}
		static CCString * create(const char * str);
	protected:
		CCString(const char * str)
		:
		_str(str)
		{
		}
	private:
		std::string _str;
	};
}

#endif // __GAF_COCOA_CCSTRING___
