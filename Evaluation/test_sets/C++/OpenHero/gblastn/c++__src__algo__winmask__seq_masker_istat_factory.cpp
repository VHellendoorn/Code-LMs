/*  $Id: seq_masker_istat_factory.cpp 359165 2012-04-11 13:45:29Z morgulis $
 * ===========================================================================
 *
 *                            PUBLIC DOMAIN NOTICE
 *               National Center for Biotechnology Information
 *
 *  This software/database is a "United States Government Work" under the
 *  terms of the United States Copyright Act.  It was written as part of
 *  the author's official duties as a United States Government employee and
 *  thus cannot be copyrighted.  This software/database is freely available
 *  to the public for use. The National Library of Medicine and the U.S.
 *  Government have not placed any restriction on its use or reproduction.
 *
 *  Although all reasonable efforts have been taken to ensure the accuracy
 *  and reliability of the software and data, the NLM and the U.S.
 *  Government do not and cannot warrant the performance or results that
 *  may be obtained by using this software or data. The NLM and the U.S.
 *  Government disclaim all warranties, express or implied, including
 *  warranties of performance, merchantability or fitness for any particular
 *  purpose.
 *
 *  Please cite the author in any work or product based on this material.
 *
 * ===========================================================================
 *
 * Author:  Aleksandr Morgulis
 *
 * File Description:
 *   Implementation of CSeqMaskerIstatFactory class.
 *
 */

#include <ncbi_pch.hpp>

#include <algo/winmask/seq_masker_istat_factory.hpp>
#include <algo/winmask/seq_masker_istat_ascii.hpp>
#include <algo/winmask/seq_masker_istat_oascii.hpp>
#include <algo/winmask/seq_masker_istat_bin.hpp>
#include <algo/winmask/seq_masker_istat_obinary.hpp>

BEGIN_NCBI_SCOPE

//------------------------------------------------------------------------------
const char * CSeqMaskerIstatFactory::Exception::GetErrCodeString() const
{
    switch( GetErrCode() )
    {
        case eBadFormat:    return "unknown format";
        case eCreateFail:   return "creation failure";
        case eOpen:         return "open failed";
        default:            return CException::GetErrCodeString();
    }
}

//------------------------------------------------------------------------------
CSeqMaskerIstat * CSeqMaskerIstatFactory::create( const string & name,
                                                  Uint4 threshold,
                                                  Uint4 textend,
                                                  Uint4 max_count,
                                                  Uint4 use_max_count,
                                                  Uint4 min_count,
                                                  Uint4 use_min_count,
                                                  bool use_ba )
{
    try
    {
        {
            CNcbiIfstream check( name.c_str() );

            if( !check )
                NCBI_THROW( Exception, eOpen, 
                            string( "could not open " ) + name );

            Uint4 data = 1;
            check.read( (char *)&data, sizeof( Uint4 ) );

            if( data == 0 )
                return new CSeqMaskerIstatBin(  name,
                                                threshold, textend,
                                                max_count, use_max_count,
                                                min_count, use_min_count );
            else if( data == 0x41414141 )
                return new CSeqMaskerIstatOAscii( name,
                                                  threshold, textend,
                                                  max_count, use_max_count,
                                                  min_count, use_min_count );
            else if( data == 1 || data == 2 ) 
                return new CSeqMaskerIstatOBinary( name,
                                                   threshold, textend,
                                                   max_count, use_max_count,
                                                   min_count, use_min_count,
                                                   use_ba );
        }

        return new CSeqMaskerIstatAscii( name,
                                         threshold, textend,
                                         max_count, use_max_count,
                                         min_count, use_min_count );
    }
    catch( CException & e)
    {
        NCBI_RETHROW( e, Exception, eCreateFail,
                      "could not create a unit counts container" );
    }
    catch( std::exception & e )
    {
        NCBI_THROW( Exception, eCreateFail,
                    std::string( "could not create a unit counts container: " ) +
                    e.what() );
    }
}

END_NCBI_SCOPE
