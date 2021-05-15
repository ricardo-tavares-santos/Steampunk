#pragma once

#ifndef __GAF_COCOA_CCAFFINE_TRANSFORM__H___
#define __GAF_COCOA_CCAFFINE_TRANSFORM__H___

#include <string>

#define CCAffineTransformMake(a, b, c, d, tx, ty) CCAffineTransform(a, b, c, d, tx, ty)

namespace GAF
{
	class CCAffineTransform : CCObject
	{
		public:
		inline CCAffineTransform()
		:
		a(1.0f), b(0.0f), c(0.0f), d(1.0f), tx(0.0f), ty(1.0f)
		{
		}		
		inline CCAffineTransform(float _a, float _b, float _c, float _d, float _tx, float _ty)
		:
		a(_a), b(_b), c(_c), d(_d), tx(_tx), ty(_ty)
		{
		}
		float a, b, c, d, tx, ty;
	};
}

#endif // __GAF_COCOA_CCAFFINE_TRANSFORM__H___
