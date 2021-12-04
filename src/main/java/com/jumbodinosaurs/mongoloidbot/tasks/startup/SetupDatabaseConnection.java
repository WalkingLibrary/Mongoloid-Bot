package com.jumbodinosaurs.mongoloidbot.tasks.startup;

import com.jumbodinosaurs.devlib.database.DataBase;
import com.jumbodinosaurs.devlib.database.DataBaseManager;
import com.jumbodinosaurs.devlib.database.DataBaseUser;
import com.jumbodinosaurs.devlib.database.exceptions.NoSuchDataBaseException;
import com.jumbodinosaurs.devlib.log.LogManager;
import com.jumbodinosaurs.devlib.task.StartUpTask;
import com.jumbodinosaurs.devlib.util.OperatorConsole;
import com.jumbodinosaurs.mongoloidbot.JDAController;

public class SetupDatabaseConnection extends StartUpTask
{
    
    private static final String mongoloidDatabaseName = "Mongoloid_Khanate_Discord";
    public static DataBase mogoloidDatabase;
    
    @Override
    public void run()
    {
        DataBaseManager.initializeDataBases(JDAController.optionsFolder);
        
        while(mogoloidDatabase == null)
        {
            try
            {
                mogoloidDatabase = DataBaseManager.getDataBase(mongoloidDatabaseName);
                if(mogoloidDatabase != null)
                {
                    LogManager.consoleLogger.info("Database Found: " + mogoloidDatabase.getURL());
                }
            }
            catch(NoSuchDataBaseException e)
            {
                LogManager.consoleLogger.error("No Database Found Named: " + mongoloidDatabaseName);
                LogManager.consoleLogger.info("Please Enter Database Details: ");
                System.out.println("Enter the database's IP:");
                String ip = OperatorConsole.getEnsuredAnswer();
                System.out.println("Enter the database's PORT:");
                String port = OperatorConsole.getEnsuredAnswer();
                System.out.println("Enter the database user's USERNAME:");
                String username = OperatorConsole.getEnsuredAnswer();
                System.out.println("Enter the database user's PASSWORD:");
                String password = OperatorConsole.getEnsuredAnswer();
                
                DataBaseUser user = new DataBaseUser(username, password);
                DataBase dataBase = new DataBase(mongoloidDatabaseName, ip, port, user);
                boolean success = DataBaseManager.addDataBase(dataBase);
                if(!success)
                {
                    LogManager.consoleLogger.error(mongoloidDatabaseName + " already exists in the DataBaseManager");
                    throw new IllegalStateException(mongoloidDatabaseName + " already exists in the DataBaseManager");
                }
                else
                {
                    LogManager.consoleLogger.info(mongoloidDatabaseName + " has been added to the DataBaseManager");
                }
            }
            
        }
    }
    
    @Override
    public int getOrderingNumber()
    {
        return 0;
    }
}
