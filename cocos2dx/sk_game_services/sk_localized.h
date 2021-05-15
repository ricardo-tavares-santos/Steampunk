//
// jphw 17.2014
//

/*
 *
 * Note that if string in localized_x file contains arabic symbol - entire string will be reversed upon parsing
 * thus you have to write all formatting sequences like "%d" in reversed order too.
 *
 * example: 
 * -- normal string "omg %d wtf \n"
 * -- arabic string "تلعب d% الآن \n"
 *
 * This rule does not apply to "\n" sequence as you can see.
 *
 */

#ifndef __SK_LOCALIZED__
#define __SK_LOCALIZED__

#include "cocoa/CCGeometry.h"

#include <string>
#include <map>

namespace cocos2d
{
    class CCLabelBMFont;
}

namespace sk
{
    class localized
    {
    public:
        static localized* shared();
        
        const char* getEnString( const char* key );
        std::string getLocString( const char* key );
        
        std::string getEnStringF( const char* key, ... );
        std::string getLocStringF( const char* key, ... );
        
        const char* getEnFontFolder( ) { return _englishFontFolder.c_str(); };
        const char* getLocFontFolder( ) { return _localizedFontFolder.c_str(); };
        
        cocos2d::CCSize getEnLabelSize( const char* englishFont, const char* key );
        cocos2d::CCSize getEnLabelSizeF( const char* englishFont, const char* key, ... );
        
        void scaleLabelToFitEng( cocos2d::CCLabelBMFont* label, const char* englishFont, const char* key );
        void scaleLabelToFitEngF( cocos2d::CCLabelBMFont* label, const char* englishFont, const char* key, ... );
        void scaleLabelToFitEng( cocos2d::CCLabelBMFont* label, const char* englishFont );

    private:
        std::map< std::string, std::string > _englishStrings;
        std::map< std::string, std::string > _localizedStrings;
        
        std::string _englishFontFolder;
        std::string _localizedFontFolder;
        
        localized() {};
        ~localized() {};
        
        void init();
    };
}

#endif // __SK_LOCALIZED__
