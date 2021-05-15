//
// jphw 17.2014
//

#include "sk_localized.h"
#include "sk_game_services/utf8.h"

#include "CCApplication.h"
#include "platform/CCFileUtils.h"
#include "ccMacros.h"
#include "label_nodes/CCLabelBMFont.h"
#include "CCDirector.h"

#include <map>
#include <sstream>
#include <iostream>

static const int kMaxStringLength = 256;

static sk::localized* localizedInstance = 0;

using namespace sk;
USING_NS_CC;

#pragma mark - Helper functions

bool isUtf8StringContainsArabicSymbols( std::string& utf8String )
{
    std::string::iterator utf8StringIterator = utf8String.begin();
    while ( utf8StringIterator != utf8String.end() )
    {
        uint32_t code = utf8::next( utf8StringIterator, utf8String.end() );
        
        if ( code >= 0x0600 && code <= 0x06FF )
            return true;
    }
    
    return false;
}

std::string getReversedUtf8String( std::string& utf8String )
{
    std::vector< uint32_t > utf8Codes;
    
    std::string::iterator utf8StringIterator = utf8String.begin();
    while ( utf8StringIterator != utf8String.end() )
    {
        utf8Codes.push_back( utf8::next( utf8StringIterator, utf8String.end()) );
    }
    
    std::string utf8StringReversed;
    
    for ( int i = utf8Codes.size() - 1; i >= 0; i-- )
    {
        utf8::append( utf8Codes.at( i ), std::back_inserter( utf8StringReversed ) );
    }
    
    return utf8StringReversed;
}

static void precacheStrings( std::map< std::string, std::string >* stringsCache, const char* fullPath )
{
    unsigned long fileSize = 0;
    unsigned char* fileData = CCFileUtils::sharedFileUtils()->getFileData( fullPath, "rb", &fileSize );;
    
    std::string fileString;
    fileString.assign( fileData, fileData + fileSize - 1 );
    
    std::istringstream fileStringStream( fileString );
    
    std::string line;
    while ( std::getline( fileStringStream, line ) )
    {
        if ( line.find( "/*", 0 ) != std::string::npos )
            continue;
        if ( line.find( "//", 0 ) != std::string::npos )
            continue;
        
        std::string::size_type equalCharPos = line.find( "=", 0 );
        if ( equalCharPos == std::string::npos )
            continue;
        
        std::string keyStr = line.substr( 0, equalCharPos );
        std::string valueStr = line.substr( equalCharPos + 1, line.size() - 1 );
        
        // trim spaces and tabs
        keyStr.erase( 0, keyStr.find_first_not_of(" \t") );
        keyStr.erase( keyStr.find_last_not_of(" \t") + 1, std::string::npos );
        
        valueStr.erase( 0, valueStr.find_first_not_of(" \t") );
        valueStr.erase( valueStr.find_last_not_of(" \t") + 1, std::string::npos );
        
        // trim commas and semicolon
        keyStr.erase( 0, keyStr.find_first_not_of("\"") );
        keyStr.erase( keyStr.find_last_not_of("\"") + 1, std::string::npos );
        
        valueStr.erase( 0, valueStr.find_first_not_of("\"") );
        valueStr.erase( valueStr.find_last_not_of("\r") + 1, std::string::npos ); // if case of
        valueStr.erase( valueStr.find_last_not_of(";") + 1, std::string::npos );
        valueStr.erase( valueStr.find_last_not_of("\"") + 1, std::string::npos );
        
        //replace "\n" with new line byte
        string old_value("\\n");
        string::size_type pos = valueStr.find( old_value );
        
        if( pos != string::npos )
        {
            for( ; pos != string::npos; pos += 1 )
            {
                pos = valueStr.find( old_value, pos );
                if( pos != string::npos )
                {
                    valueStr.erase( pos, 2 );
                    valueStr.insert( pos, 1, '\n' );
                }
                else
                    break;
            }
        }
        
        if ( isUtf8StringContainsArabicSymbols( valueStr ) )
            (*stringsCache)[ keyStr ] = getReversedUtf8String( valueStr );
        else
            (*stringsCache)[ keyStr ] = valueStr;
    }
    
    if ( fileData ) {
        delete [] fileData;
    }
}

#pragma mark - sk::localized

localized* localized::shared()
{
    if ( localizedInstance )
        return localizedInstance;
    else
    {
        localizedInstance = new localized();
        localizedInstance->init();
        return localizedInstance;
    }
}

void localized::init()
{
    // check if english localization file exist and parse it
    std::string enFileName = CCFileUtils::sharedFileUtils()->fullPathForFilename( "localized/localized_en" );
    
    CCAssert( CCFileUtils::sharedFileUtils()->isFileExist( enFileName ), "English localization file not found" );
    
    precacheStrings( &_englishStrings, enFileName.c_str() );
    _englishFontFolder = "fonts/en/";
    
    // detect current language and try to parse related localization file
    ccLanguageType curLanguage = CCApplication::sharedApplication()->getCurrentLanguage();
    
    std::string curFileName;
    
    // for debug
    //curLanguage = kLanguagePersian;
    // for debug end
    
    switch (curLanguage) {
        case kLanguageEnglish:
            // if locales is english then thats it 
            _localizedStrings = _englishStrings;
            _localizedFontFolder = _englishFontFolder;
            return;
            
        case kLanguageChinese:
            curFileName = CCFileUtils::sharedFileUtils()->fullPathForFilename( "localized/localized_zh" );
            _localizedFontFolder = "fonts/zh/";
            break;
            
        case kLanguageRussian:
            curFileName = CCFileUtils::sharedFileUtils()->fullPathForFilename( "localized/localized_ru" );
            _localizedFontFolder = "fonts/ru/";
            break;
            
        case kLanguageRomanian:
            curFileName = CCFileUtils::sharedFileUtils()->fullPathForFilename( "localized/localized_ro" );
            _localizedFontFolder = "fonts/ro/";
            break;
            
        case kLanguageSpanish:
            curFileName = CCFileUtils::sharedFileUtils()->fullPathForFilename( "localized/localized_es" );
            _localizedFontFolder = "fonts/es/";
            break;
            
        case kLanguagePortuguese:
            curFileName = CCFileUtils::sharedFileUtils()->fullPathForFilename( "localized/localized_pt" );
            _localizedFontFolder = "fonts/pt/";
            break;
            
        case kLanguageItalian:
            curFileName = CCFileUtils::sharedFileUtils()->fullPathForFilename( "localized/localized_it" );
            _localizedFontFolder = "fonts/it/";
            break;
            
        case kLanguagePolish:
            curFileName = CCFileUtils::sharedFileUtils()->fullPathForFilename( "localized/localized_pl" );
            _localizedFontFolder = "fonts/pl/";
            break;
            
        case kLanguageGerman:
            curFileName = CCFileUtils::sharedFileUtils()->fullPathForFilename( "localized/localized_de" );
            _localizedFontFolder = "fonts/de/";
            break;
            
        case kLanguageTurkish:
            curFileName = CCFileUtils::sharedFileUtils()->fullPathForFilename( "localized/localized_tr" );
            _localizedFontFolder = "fonts/tr/";
            break;
            
        case kLanguageFrench:
            curFileName = CCFileUtils::sharedFileUtils()->fullPathForFilename( "localized/localized_fr" );
            _localizedFontFolder = "fonts/fr/";
            break;
            
        case kLanguageJapanese:
            curFileName = CCFileUtils::sharedFileUtils()->fullPathForFilename( "localized/localized_ja" );
            _localizedFontFolder = "fonts/ja/";
            break;
            
        case kLanguageKorean:
            curFileName = CCFileUtils::sharedFileUtils()->fullPathForFilename( "localized/localized_ko" );
            _localizedFontFolder = "fonts/ko/";
            break;
            
        case kLanguageVietnameze:
            curFileName = CCFileUtils::sharedFileUtils()->fullPathForFilename( "localized/localized_vi" );
            _localizedFontFolder = "fonts/vi/";
            break;
            
        case kLanguageHindi:
            curFileName = CCFileUtils::sharedFileUtils()->fullPathForFilename( "localized/localized_hi" );
            _localizedFontFolder = "fonts/hi/";
            break;
            
        case kLanguageMalay:
            curFileName = CCFileUtils::sharedFileUtils()->fullPathForFilename( "localized/localized_ms" );
            _localizedFontFolder = "fonts/ms/";
            break;
            
        case kLanguageJavanese:
            curFileName = CCFileUtils::sharedFileUtils()->fullPathForFilename( "localized/localized_jv" );
            _localizedFontFolder = "fonts/jv/";
            break;
            
        case kLanguageBengali:
            curFileName = CCFileUtils::sharedFileUtils()->fullPathForFilename( "localized/localized_bn" );
            _localizedFontFolder = "fonts/bn/";
            break;
            
        case kLanguageThai:
            curFileName = CCFileUtils::sharedFileUtils()->fullPathForFilename( "localized/localized_th" );
            _localizedFontFolder = "fonts/th/";
            break;
            
        case kLanguageArabic:
            curFileName = CCFileUtils::sharedFileUtils()->fullPathForFilename( "localized/localized_ar" );
            _localizedFontFolder = "fonts/ar/";
            break;
        
        case kLanguagePersian:
            curFileName = CCFileUtils::sharedFileUtils()->fullPathForFilename( "localized/localized_fn" );
            _localizedFontFolder = "fonts/fn/";
            break;
            
        default:
            curFileName = CCFileUtils::sharedFileUtils()->fullPathForFilename( "localized/localized_en" );
            _localizedFontFolder = "fonts/en/";
            break;
    }
    
    if ( CCFileUtils::sharedFileUtils()->isFileExist( curFileName ) )
    {
        precacheStrings( &_localizedStrings, curFileName.c_str() );
    }
    else
    {
        // if localization file not found - just use english one
        _localizedStrings = _englishStrings;
        _localizedFontFolder = _englishFontFolder;
    }
}

const char* localized::getEnString( const char *key )
{
    if ( _englishStrings.find( key ) != _englishStrings.end() )
    {
        return _englishStrings[ key ].c_str();
    }
    
    return "unknown key";
}

std::string localized::getLocString( const char *key )
{
    if ( _localizedStrings.find( key ) != _localizedStrings.end() )
    {

        return _localizedStrings[ key ];
    }
    
    return "unknown key";
}

std::string localized::getEnStringF( const char *key, ... )
{
    if ( _englishStrings.find( key ) != _englishStrings.end() )
    {
        char buffer[ kMaxStringLength ];
        
        va_list args;
        
        va_start ( args, key );
        vsprintf ( buffer, _englishStrings[ key ].c_str(), args );
        va_end ( args );
        
        std::string formattedString = buffer;
        
        return formattedString;
    }
    
    return "unknown key";
}

std::string localized::getLocStringF( const char *key, ... )
{
    if ( _localizedStrings.find( key ) != _localizedStrings.end() )
    {
        char buffer[ kMaxStringLength ];
        
        va_list args;
        
        va_start ( args, key );
        
        vsprintf ( buffer, _localizedStrings[ key ].c_str(), args );

        va_end ( args );
        
        std::string formattedString = buffer;
        
        return formattedString;
    }
    
    return "unknown key";
}

CCSize localized::getEnLabelSize( const char* englishFont, const char* key )
{
    CCSize winSize = CCDirector::sharedDirector()->getWinSize();
    
    CCLabelBMFont* enLabel = CCLabelBMFont::create( getEnString( key ), englishFont, winSize.width * 2, kCCTextAlignmentCenter );

    return enLabel->getContentSize();
}

CCSize localized::getEnLabelSizeF( const char* englishFont, const char* key, ... )
{
    CCSize winSize = CCDirector::sharedDirector()->getWinSize();
    
    va_list args;
    
    va_start ( args, key );
    std::string str = getEnStringF( key, args );
    va_end ( args );
    
    CCLabelBMFont* enLabel = CCLabelBMFont::create( str.c_str(), englishFont, winSize.width * 2, kCCTextAlignmentCenter );
    
    return enLabel->getContentSize();
}

void localized::scaleLabelToFitEng( cocos2d::CCLabelBMFont *label, const char* englishFont, const char* key )
{
    float oldScale = label->getScale();
    
    float englishLabelWidth = getEnLabelSize( englishFont, key ).width;
    float localizedLabelWidth = label->getContentSize().width;

    if ( englishLabelWidth < localizedLabelWidth )
        label->setScale( (englishLabelWidth / localizedLabelWidth) * oldScale );
}

void localized::scaleLabelToFitEngF( cocos2d::CCLabelBMFont *label, const char* englishFont, const char* key, ... )
{
    float oldScale = label->getScale();
    
    va_list args;
    
    va_start ( args, key );
    float englishLabelWidth = getEnLabelSizeF( englishFont, key, args ).width;
    float localizedLabelWidth = label->getContentSize().width;

    label->setScale( (englishLabelWidth / localizedLabelWidth) * oldScale );
    va_end ( args );
}

void localized::scaleLabelToFitEng( CCLabelBMFont *label, const char *englishFont )
{
    CCSize winSize = CCDirector::sharedDirector()->getWinSize();
    
    CCLabelBMFont* enLabel = CCLabelBMFont::create( label->getString(), englishFont, winSize.width * 2, kCCTextAlignmentCenter );
    
    float oldScale = label->getScale();
    
    float englishLabelWidth = enLabel->getContentSize().width;
    float localizedLabelWidth = label->getContentSize().width;
    
    if ( englishLabelWidth < localizedLabelWidth )
        label->setScale( (englishLabelWidth / localizedLabelWidth) * oldScale );
}