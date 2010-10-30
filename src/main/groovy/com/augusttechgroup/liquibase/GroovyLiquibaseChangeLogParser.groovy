//
// Groovy Liquibase ChangeLog
//
// Copyright (C) 2010 Tim Berglund
// http://augusttechgroup.com
// Littleton, CO
//
// Licensed under the GNU Lesser General Public License v2.1
//

package com.augusttechgroup.liquibase

import liquibase.parser.ChangeLogParser
import liquibase.changelog.DatabaseChangeLog
import liquibase.changelog.ChangeLogParameters
import liquibase.resource.ResourceAccessor
import liquibase.exception.ChangeLogParseException


class GroovyLiquibaseChangeLogParser
  implements ChangeLogParser {


  DatabaseChangeLog parse(String physicalChangeLogLocation,
                          ChangeLogParameters changeLogParameters,
                          ResourceAccessor resourceAccessor) {

    def inputStream = resourceAccessor.getResourceAsStream(physicalChangeLogLocation)
    if(!inputStream) {
        throw new ChangeLogParseException(physicalChangeLogLocation + " does not exist")
    }

    try {
      def changeLog = new DatabaseChangeLog(physicalChangeLogLocation)
      changeLog.setChangeLogParameters(changeLogParameters)

      def binding = new Binding()
      def shell = new GroovyShell(binding)

      // Parse the script, give it the local changeLog instance, give it access
      // to root-level method delegates, and call.
      def script = shell.parse(inputStream)
      script.metaClass.getDatabaseChangeLog = { -> changeLog }
      script.metaClass.methodMissing = changeLogMethodMissing
      script.run()
      
      // The changeLog will have been populated by the script
      return changeLog
    }
    finally {
      try {
        inputStream.close()
      }
      catch(Exception e) {
        // Can't do much more than hope for the best here
      }
    }
  }


  boolean supports(String changeLogFile, ResourceAccessor resourceAccessor) {
    changeLogFile.endsWith('.groovy')
  }


  int getPriority() {
    PRIORITY_DEFAULT
  }


  def getChangeLogMethodMissing() {
    { name, args ->
      switch(name) {
        case 'databaseChangeLog':
          
          break
          
        case 'preConditions':
          break
          
        case 'include':
          break
          
        case 'changeSet':
          break
          
        default:
          throw new ChangeLogParseException("Unrecognized root element ${name}")
          break
      }
    }
  }


  class DatabaseChangeLogDelegate {
    
  }

}
