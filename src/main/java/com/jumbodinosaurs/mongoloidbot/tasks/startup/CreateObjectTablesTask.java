package com.jumbodinosaurs.mongoloidbot.tasks.startup;

import com.jumbodinosaurs.devlib.database.DataBaseUtil;
import com.jumbodinosaurs.devlib.database.Query;
import com.jumbodinosaurs.devlib.database.objectHolder.SQLStoreObject;
import com.jumbodinosaurs.devlib.log.LogManager;
import com.jumbodinosaurs.devlib.reflection.ReflectionUtil;
import com.jumbodinosaurs.devlib.task.StartUpTask;

import java.sql.SQLException;

public class CreateObjectTablesTask extends StartUpTask
{
    @Override
    public void run()
    {
        /*
         * Process for creating each needed table for the Post System
         * Get/Check for Server DataBase
         * Create a table for the tables
         * Create a table for each PostObject
         *
         *
         *  */
        
        
        if(SetupDatabaseConnection.mogoloidDatabase == null)
        {
            throw new IllegalStateException("No Database Set");
        }
        
        
        //Create a table for the tables
        String statement = "create table IF NOT EXISTS %s (" +
                           "id int primary key auto_increment," +
                           "objectJson json not null" +
                           ");";
        
        //Create a table for each PostObject
        
        for(Class clazz : ReflectionUtil.getSubClasses(SQLStoreObject.class, true, false))
        {
            String tableName = clazz.getSimpleName();
            String createTableStatement = String.format(statement, tableName);
            Query query = new Query(createTableStatement);
            try
            {
                DataBaseUtil.manipulateDataBase(query, SetupDatabaseConnection.mogoloidDatabase);
            }
            catch(SQLException e)
            {
                LogManager.consoleLogger.error(e.getMessage());
            }
        }
        
    }
    
    
    @Override
    public int getOrderingNumber()
    {
        return 1;
    }
}
