package org.usfirst.frc.team228.robot;

import com.ctre.CANTalon; //we will use this later
import com.ctre.CANTalon.FeedbackDevice;
import com.ctre.CANTalon.TalonControlMode;

import edu.wpi.first.wpilibj.Compressor;
import edu.wpi.first.wpilibj.DoubleSolenoid;
import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.Encoder;
//import edu.wpi.first.wpilibj.GenericHID.Hand; //probably don't need
import edu.wpi.first.wpilibj.IterativeRobot;
//import edu.wpi.first.wpilibj.Joystick; //probably don't need
import edu.wpi.first.wpilibj.RobotDrive;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj.Solenoid;
import edu.wpi.first.wpilibj.TalonSRX;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.VictorSP; //not victor like we thought
import edu.wpi.first.wpilibj.XboxController;

/**
 * The VM is configured to automatically run this class, and to call the
 * functions corresponding to each mode, as described in the IterativeRobot
 * documentation. If you change the name of this class or the package after
 * creating this project, you must also update the manifest file in the resource
 * directory.
 */
public class Robot extends IterativeRobot
{
	//PRE INIT - Create variables
	
	//ATTENTION!
	//set to true for 2016 robot, false for either 2017 robot:
	final boolean is2016Robot = true;
	
	//Auto Selection
	final String defaultAuto = "Do nothing";
	final String customAuto = "Custom Auto";
	//user's autonomous selection
	String autoSelected;
	//autonomous selector
	SendableChooser<String> autoChooser; //new
	//SendableChooser autoChooser; //old
	
	//Teleop Drive Mode Selection
	//Strings for each particular mode
	final String arcadeMode = "Arcade";
	final String tankMode = "Tank";
	final String GTAMode = "GTA";
	//user's drive mode selection
	String driveMode;
	//int driveTrainId;
	//drive mode selector
	SendableChooser<String> driveChooser; //old
	//SendableChooser driveChooser; //old
	
	//Compressor
	Compressor compressor;
	
	//Drivetrain
	//drive motor controllers
	VictorSP leftDrive1, leftDrive2, rightDrive1, rightDrive2;
	//encoders
	Encoder leftDriveEncoder, rightDriveEncoder;
	//pneumatics (shifters)
	Solenoid leftShifter, rightShifter;
		
	//Drive Function
	RobotDrive drivetrain;
	
	//XBoxControllers: driver: driving, operator: functions (like intake and shooter)
	XboxController driverController, operatorController;

	//Gear Manipulation
	//pincher
	DoubleSolenoid pincher;
	boolean pincherState; //when true, will pinch 
	boolean pincherButtonPrev; //state of the button from last iteration
	//rotator, moves gear up/down
	Solenoid gearRotator; 
	boolean gearRotatorState; //when true, will hold gear down
	boolean gearRotatorButtonPrev; //state of the button from last iteration
	//sensors
	DigitalInput gearDetectionLimitSwitch; //not currently initialized
	
	//Ball Manipulation
	//belt motor controllers
	VictorSP intakeBelt, feederBelt;
	//human load gate
	Solenoid HLGate;
	boolean HLGateState; //when true, will open the human load gate 
	boolean HLGateButtonPrev; //state of the button from last iteration
	//dumper gate
	Solenoid dumperGate;
	boolean dumperState; //when true, will open the dumper gate
	boolean dumperButtonPrev; //state of the button from last iteration

	//Shooter
	//controllers (Talons)
	CANTalon shooterMotor1,shooterMotor2,shooterMotor3;
	boolean shooterState;
	boolean shooterButtonPrev;
	//constant for shooter speed
	double OLShooterValue = 0.7; //no longer a constant //"open loop shooter value"
	//sensors
	//***insert sensor code here***
	
	//Hanging
	//motor controllers
	VictorSP hangingWinch;
	//variables
	boolean hangFeedForward; //when true, will apply feed forward value to hanger 
	boolean hangButtonPrev; //state of the button from last iteration
	
	//Checks if auto is on
	boolean inAuto; //removed "= true", because it shouldn't default to true
	
	//Counts time elapsed while in autonomous
	Timer autoTimer;
	
	//example for toggling buttons
	//boolean exampleState; //when true, will do the thing on the robot 
	//boolean exampleButtonPrev; //state of the button from last iteration
	
	/**
	 * This function is run when the robot is first started up and should be
	 * used for any initialization code.
	 */
	public void robotInit()
	{
	//Robot Init - Put Things on SmartDashboard and Assign Everything
		
		//Shooter
		//put shooter constant data
		//SmartDashboard.putNumber("Shooter constant", OLShooterValue);
		//get user input for constant, assign to OLShooterValue
		//puts a boolean (which becomes checkbox) on SmartDashboard
		//SmartDashboard.putBoolean("Shooter PID on", false);
		//display F, P, I, and D
		SmartDashboard.putNumber("Shooter F", shooterMotor1.getF());
		SmartDashboard.putNumber("Shooter P", shooterMotor1.getP());
		SmartDashboard.putNumber("Shooter I", shooterMotor1.getI());
		SmartDashboard.putNumber("Shooter D", shooterMotor1.getD());
		
		//Assign Chooser for Autonomous programs
		autoChooser = new SendableChooser<String>();
		//autoChooser = new SendableChooser();
				
		//Assign Chooser for Teleop Drive Mode
		driveChooser = new SendableChooser<String>();
		//driveChooser = new SendableChooser();
		
		//Put Autonomous Chooser
		autoChooser.addDefault("Auto nothing", defaultAuto);
		autoChooser.addObject("Auto custom", customAuto);
		SmartDashboard.putData("Auto Choices", autoChooser);

		//Put Teleop Drive Mode Chooser
		driveChooser.addDefault("Tank Drive", tankMode); //consider simplifying this process
		driveChooser.addObject("Arcade Drive", arcadeMode);
		driveChooser.addObject("GTA Drive", GTAMode);
		SmartDashboard.putData("Drive Choices", driveChooser);

		//Autonomous mode timer
		autoTimer = new Timer();
		
		//Assign Compressor
		compressor = new Compressor();
		
		//Drivetrain
		//Assign drive motor controllers
		if (is2016Robot)
		{
			leftDrive1 = new VictorSP(0);
			leftDrive2 = new VictorSP(1);
			rightDrive1 = new VictorSP(3);
			rightDrive2 = new VictorSP(4);
		} 
		else 
		{
			leftDrive1 = new VictorSP(0);
			leftDrive2 = new VictorSP(1);
			rightDrive1 = new VictorSP(2);
			rightDrive2 = new VictorSP(3);
		}
		//Assign drivetrain shifters
		leftShifter = new Solenoid(0);
		rightShifter = new Solenoid(1);

		//Assign Drive Function
		drivetrain = new RobotDrive(leftDrive1, leftDrive2, rightDrive1, rightDrive2);
		
		//Assign XboxControllers
		driverController = new XboxController(0);
		operatorController = new XboxController(1);
		
		//Gear Manipulation
		//Assign Pincher
		pincher = new DoubleSolenoid(2,3);
		pincherState = false;
		pincherButtonPrev = false;
		//Assign Rotator
		gearRotator = new Solenoid(4);
		gearRotatorState = false;
		gearRotatorButtonPrev = false;
		
		//Ball Manipulation
		//Assign belt motor controllers
		if (is2016Robot)
		{
			intakeBelt = new VictorSP(7); //ball intake
			feederBelt = new VictorSP(8); //shooter feed belt
		}
		else
		{
			intakeBelt = new VictorSP(4); //ball intake
			feederBelt = new VictorSP(5); //shooter feed belt
		}
		//Assign Human Load gate
		HLGate = new Solenoid(5);
		HLGateState = false;
		HLGateButtonPrev = false;
		//Assign Dumper Gate
		dumperGate = new Solenoid(6);
		dumperState = false;
		dumperButtonPrev = false;
		
		//Shooter
		//Assign Shooter motor controllers
		if (!is2016Robot)
		{
			shooterMotor1 = new CANTalon(1); //verify IDs
			shooterMotor2 = new CANTalon(2); 
			shooterMotor3 = new CANTalon(3);
			
			shooterMotor1.setFeedbackDevice(FeedbackDevice.QuadEncoder);
			//set other Talons to follow - verify correct numbers for everything
			shooterMotor2.changeControlMode(CANTalon.TalonControlMode.Follower);
			shooterMotor2.set(shooterMotor1.getDeviceID());
			shooterMotor3.changeControlMode(CANTalon.TalonControlMode.Follower);
			shooterMotor3.set(shooterMotor1.getDeviceID());
			
			//set nominal and peak voltage, 12V means full (but only here!)
			shooterMotor1.configNominalOutputVoltage(0.0, -0.0);
			shooterMotor1.configPeakOutputVoltage(12.0, -12.0);
			
			//internet told me to put this here for now?
			shooterMotor1.setProfile(0);
			shooterMotor1.setF(0);
			shooterMotor1.setP(0);
			shooterMotor1.setI(0);
			shooterMotor1.setD(0);
		
		}
		shooterButtonPrev = false;
		shooterState = false;
		
		//Assign Hanging motor controllers
		hangingWinch = new VictorSP(6);
		hangFeedForward = false;
		hangButtonPrev = false;
		
		//set example mechanism and button statuses to false
		//exampleState = false;
		//exampleButtonPrev = false;
	}
	
	/**
	 * This function is called each time a new packet is received from the driver station
	 * (approximately every 20ms).
	 * Periodic code for all robot modes should go here.
	 */
	public void robotPeriodic()
	{
		//Get shooter constant from user on SmartDashboard
		//OLShooterValue = SmartDashboard.getNumber("Shooter constant", OLShooterValue);
		SmartDashboard.putData("Auto Choices", autoChooser);
		SmartDashboard.putData("Drive Choices", driveChooser);

	}
	
	/**
	 * This autonomous (along with the chooser code above) shows how to select between different autonomous modes
	 * using the dashboard. The sendable chooser code works with the Java SmartDashboard. 
	 *
	 * You can add additional auto modes by adding additional comparisons to the switch structure below with additional strings.
	 * If using the SendableChooser make sure to add them to the chooser code above as well.
	 */
	public void autonomousInit()
	{
		//get Autonomous selection
		autoSelected = (String) autoChooser.getSelected();
		//print autonomous selection
		System.out.println("Auto selected: " + autoSelected);
		
		autoTimer.reset();
		autoTimer.start();
		
		//inAuto = true;
	}

	/**
	 * This function is called periodically during autonomous
	 */
	public void autonomousPeriodic()
	{
		switch(autoSelected)
		{
		case customAuto:
			//CustomAuto(); //commenting out because we're rewriting this function
			testAuto();
			//Put custom auto code here   
			break;
		case defaultAuto:
		default:
			drivetrain.arcadeDrive(0.0, 0.0); //prevents watchdog error
			//Put default auto code here
			break;
		}
	}
	/**Haley and Chris Auto Test
	 * designed to be identical in behavior to CustomAuto 
	 * but written in a more robust way as an example for programming team
	*/
	
	public void testAuto()
	{
		//Timer.getMatchTime() wasn't working, is now autoTimer.get()
		//0s: forward
		//2s: backward
		//4s: stop
		
		//at 0s: drive forward for 2 seconds
		//if (Timer.getMatchTime() < 2)
		if (autoTimer.get() < 2)
		{
			drivetrain.arcadeDrive(1.0, 0.0);
		}
		//at 2s: drive backwards for 2 seconds
		//else if (Timer.getMatchTime() < 4)
		else if (/*autoTimer.get() > 2 && */autoTimer.get() < 4)
		{
			drivetrain.arcadeDrive(0.5, 0.0);
		}
		//at 4s: stop
		else //(autoTimer.get() > 4)
		{
			drivetrain.arcadeDrive(0.0, 0.0);
		}
	}
	
	/**
	 * This function is called once at the beginning of operator control
	 */
	public void teleopInit()
	{
		//get drive mode selection (tank, arcade, GTA)
		//driveMode = (String) driveChooser.getSelected();
		//print drive mode selection
		System.out.println("Drive mode selected: " + driveMode);		
	}

	/**
	 * This function is called periodically during operator control
	 */
	public void teleopPeriodic()
	{
		//don't put print statements in periodic; they would print repeatedly
		
		//display the shooter constant (OLShooterValue)
		//SmartDashboard.putNumber("Shooter constant copy", OLShooterValue);
		
		//DRIVER CONTROLS
		//Value for the GTA Mode arcade function and SmartDashboard data
		double combinedTriggerValue;
		
		//Assign drive mode selection to driveMode
		driveMode = (String)driveChooser.getSelected();
		
		//Start drive mode
		switch(driveMode)
		{
		case arcadeMode:
			drivetrain.arcadeDrive(driverController, 1, driverController, 4);
			break;
		case tankMode:
			drivetrain.tankDrive(driverController, 1, driverController, 5);
			break;
		case GTAMode:
		default:
			combinedTriggerValue = (-1 * driverController.getRawAxis(2) + driverController.getRawAxis(3));
			drivetrain.arcadeDrive(combinedTriggerValue, driverController.getRawAxis(0));
			
			//display combinedTriggerValue
			SmartDashboard.putNumber("GTADriveValue", combinedTriggerValue);

			break;
		}
		
		//the code below duplicates what is already shown on the driver station?
		SmartDashboard.putNumber("XAxisRightJoystick", driverController.getRawAxis(4));
		SmartDashboard.putNumber("YAxisLeftJoystick", driverController.getRawAxis(1));
		
		//OPERATOR CONTROLS
		if (!is2016Robot)
		{
			//Pincher: toggle  with B (DoubleSolenoid.Value.kForward = closed, ...kReverse = open)
			pincherControl(operatorController.getBButton());

			//Gear Rotator: toggle with A (on/down)/(DEFAULT: off/up)
			gearRotatorControl(operatorController.getAButton());

			//Ball Intake: left trigger
			intakeBalls(operatorController.getRawAxis(2));
			
			//Ball Feeding: left joystick y axis
			feedBalls(operatorController.getRawAxis(1));

			//Human Load Gate: toggle with Y (on/open)/(DEFAULT: off/closed)
			HLGateControl(operatorController.getYButton());
			
			//Dumper Gate: toggle with right bumper (6) (on/open/shoot)/(DEFAULT: off/closed/no shoot)
			dumperGateControl(operatorController.getRawButton(6));

			//Shooter: spin up/down toggle with left bumper
			shooterControl(operatorController.getRawButton(5), SmartDashboard.getBoolean("Shooter PID on", false)); //set to true for PID

			//Hanging: right trigger passes for throttle value, X button toggles feed-forward on and off
			hangingControl(operatorController.getRawAxis(3), operatorController.getXButton());
			}
	}
	
	/**
	 * This function is called periodically during test mode
	 */
	public void testPeriodic()
	{
		
	}

	/**
	 * This function toggles the gear pincher open/closed using a button
	 * @param pincherButton
	 */
	public void pincherControl(boolean pincherButton)
	{
		//if the button is pressed, and if it changed state
		if (pincherButton && pincherButton != pincherButtonPrev)
		{ 
			//toggle the state of pincher
			pincherState = !pincherState;
		}
		//now that check is complete, store value of pincherButton for next iteration of loop
		pincherButtonPrev = pincherButton;

		if (pincherState)
		{
			//ON: pinching/closed
			pincher.set(DoubleSolenoid.Value.kForward);
		}
		else
		{
			//OFF: release/open
			pincher.set(DoubleSolenoid.Value.kReverse);
		}
	}

	/**
	 * This function toggles the gear rotator up/down using a button
	 * @param exampleButton
	 */
	public void gearRotatorControl(boolean gearRotatorButton)
	{
		//if the button is pressed, and if it changed state
		if (gearRotatorButton && gearRotatorButton != gearRotatorButtonPrev)
		{ 
			//toggle the state of gearRotator
			gearRotatorState = !gearRotatorState;
		}
		//now that check is complete, store value of gearRotatorButton for next iteration of loop
		gearRotatorButtonPrev = gearRotatorButton;

		if (gearRotatorState)
		{
			//ON: gear rotator down
			gearRotator.set(true);
		}
		else
		{
			//OFF: gear rotator up
			gearRotator.set(false);
		}
	}	

	/** 
	* This function controls the belt mechanisms when picking up off the floor
	* intakeSpeed is expected to be between 0 and 1
	* This allows a trigger value to be passed into it, for example
	*/
	public void intakeBalls(double intakeSpeed)
	{
		//intake should go in, feeder should go OUT 
		//since intake and feeder gearboxes run opposite, pass the SAME value to both
		intakeBelt.set(intakeSpeed);
		feederBelt.set(-1 * intakeSpeed);
	}
	
	/** 
	* This function controls the belt mechanisms when feeding the shooter
	* feedSpeed is expected to be between -1 and 1
	* This allows a joystick value to be passed into it, for example
	* @param feedSpeed
	*/
	public void feedBalls(double feedSpeed)
	{
		//intake and feeder gearboxes run in opposite directions, so one must be reversed
		//if this is backward, move -1 component to other one
		intakeBelt.set(feedSpeed);
		feederBelt.set(-1 * feedSpeed);
	}

	/**
	 * This function toggles the human load gate using a button
	 * @param HLGateButton
	 */
	public void HLGateControl(boolean HLGateButton)
	{
		//if the button is pressed, and if it changed state
		if (HLGateButton && HLGateButton != HLGateButtonPrev)
		{ 
			//toggle the state of HLGate
			HLGateState = !HLGateState;
		}
		//now that check is complete, store value of HLGateButton for next iteration of loop
		HLGateButtonPrev = HLGateButton;

		if (HLGateState)
		{
			//ON: open/no shoot state
			HLGate.set(true);
		}
		else
		{
			//OFF: closed/shoot state
			HLGate.set(false);
		}
	}

	/**
	 * This function toggles the dumper gate using a button
	 * @param dumperButton
	 */
	public void dumperGateControl(boolean dumperButton)
	{
		//if the button is pressed, and if it changed state
		if (dumperButton && dumperButton != dumperButtonPrev)
		{ 
			//toggle the state of dumperGate
			dumperState = !dumperState;
		}
		//now that check is complete, store value of dumperButton for next iteration of loop
		dumperButtonPrev = dumperButton;

		if (dumperState)
		{
			//dumperGate open/no shoot state
			dumperGate.set(true);
		}
		else
		{
			//dumperGate closed/shoot state
			dumperGate.set(false);
		}
	}
	
	/**
	 * This function handles the shooter motors on the 2017 robot.
	 * @param shooterButton, isPID
	 */
	public void shooterControl(boolean shooterButton, boolean isPID)
	{
		//get user input for constant, assign to OLShooterValue
		OLShooterValue = SmartDashboard.getNumber("Shooter constant", OLShooterValue);
			
		if(shooterButton && shooterButton != shooterButtonPrev)
		{
			shooterState = !shooterState;
		}
		
		shooterButtonPrev = shooterButton;
		if (shooterState) //if the shooter has toggled on
		{
			if (isPID)
			{
				double shooterTargetSpeed = 4000.0; //4000 rpm
				shooterMotor1.changeControlMode(TalonControlMode.Speed);
				//speed setpoint multiplied by the gear ratio to get encoder speed
				shooterMotor1.set(shooterTargetSpeed * 3.625); 
				
				//send data to smartdashboard
				SmartDashboard.putNumber("Shooter Error", shooterMotor1.getClosedLoopError());
				SmartDashboard.putNumber("Shooter Signal", shooterMotor1.getOutputVoltage() / shooterMotor1.getBusVoltage());
				//not sure if output voltage needs to be divided by 12 or not
				
				//get user input for FPID and i have no idea what im doing
				shooterMotor1.set(SmartDashboard.getNumber("Shooter F", shooterMotor1.getF()));
				shooterMotor1.set(SmartDashboard.getNumber("Shooter P", shooterMotor1.getP()));
				shooterMotor1.set(SmartDashboard.getNumber("Shooter I", shooterMotor1.getI()));
				shooterMotor1.set(SmartDashboard.getNumber("Shooter D", shooterMotor1.getD()));
			}
			else //if pid is unchecked
			{
				//change talon to open-loop mode, set a value
				shooterMotor1.changeControlMode(TalonControlMode.PercentVbus);
				shooterMotor1.set(OLShooterValue);
			}
		}
		else
		{
			//change talon to open-loop mode, set to off
			shooterMotor1.changeControlMode(TalonControlMode.PercentVbus);
			shooterMotor1.set(0.0);
		}
	}
	
	/** 
	* This function controls the hanging winch and feed forward
	* hangingSpeed is expected to be between 0 and 1
	* This allows a trigger value to be passed into it, for example
	* If this is changed to a joystick, code will still work, but will allow hanger to run in reverse
	*
	* When the feed forward is enabled, hanger will stall at small voltage even without user input
	* This is designed to assist with scoring the bonus points at the end of the match
	*/
	public void hangingControl(double hangingSpeed, boolean ffButton)
	{
		final double hangFFValue = 0.1; //change this to change how much FF to apply
		
		//first check to see if the feed forward button has been pressed AND if it changed state
		//otherwise you would rapidly alternate between ff being on and off as long as the button was pressed!
		if (ffButton != hangButtonPrev && ffButton) 
		{ 
			//toggle the state of hang feed forward
			hangFeedForward = !hangFeedForward;
		}
		//now that check is complete, store value of button for next iteration of loop
		hangButtonPrev = ffButton;
		
		//if feed forward is on, apply it to the input
		if (hangFeedForward) 
		{
			hangingSpeed += hangFFValue;
		}
		//if resulting value is >1.0, reduce it
		if (hangingSpeed > 1.0) 
		{
			hangingSpeed = 1.0;
		}
		
		//actually do the hanging motor command here
		//if you need to invert this, also invert the hangFFValue constant above
		hangingWinch.set(hangingSpeed);
	}
	
	/**
	 * This is an example function for toggling when you press a button
	 * 
	 * It takes the the value of whatever button you want to use
	 * and it does the thing with the mechanism.
	 * 
	 * create global variables boolean exampleState, exampleButtonPrev in pre-init
	 * set exampleState, exampleButtonPrev to beginning state in robot init, typically false
	 * 
	 * to call this method:
	 * exampleMechanismControl(operatorController.getZButton());
	 */
	/*
	public void exampleMechanismControl(boolean exampleButton)
	{
		//if the button is pressed, and if it changed state
		if (exampleButton && exampleButton != exampleButtonPrev)
		{ 
			//toggle the state of mechanism
			exampleState = !exampleState;
		}
		//now that check is complete, store value of exampleButton for next iteration of loop
		exampleButtonPrev = exampleButton;

		if (exampleState)
		{
			//mechanism ON state
			exampleMechanism.set(true);
		}
		else
		{
			//mechanism OFF state
			exampleMechanism.set(false);
		}
	}
	*/
}