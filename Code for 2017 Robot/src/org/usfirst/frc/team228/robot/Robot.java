package org.usfirst.frc.team228.robot;

import com.ctre.CANTalon; 
import com.ctre.CANTalon.FeedbackDevice;
import com.ctre.CANTalon.TalonControlMode;
import edu.wpi.first.wpilibj.ADXRS450_Gyro;
import edu.wpi.first.wpilibj.CameraServer;
import edu.wpi.first.wpilibj.Compressor;
import edu.wpi.first.wpilibj.DoubleSolenoid;
import edu.wpi.first.wpilibj.Encoder;
import edu.wpi.first.wpilibj.IterativeRobot;
import edu.wpi.first.wpilibj.RobotDrive;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj.Solenoid;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.VictorSP; //not victor like we thought
import edu.wpi.first.wpilibj.XboxController;
import edu.wpi.first.wpilibj.Preferences;

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
	final boolean isPracticeRobot = false; //for later
	
	Preferences robotPrefs;
	
	//Auto Selection
	final String defaultAuto = "Drive Forward";
	final String centerGearAuto = "Center Gear Auto";
	final String gyroAuto = "Gyro Test Auto";
	final String rightGearAuto = "Right Gear Auto";
	final String leftGearAuto = "Left Gear Auto";
	//user's autonomous selection
	String autoSelected;
	//autonomous selector
	SendableChooser<String> autoChooser; //new
	//SendableChooser autoChooser; //old
	
	//Speed limit
	double previousInput;
	final double rateLimit = 2.0;
	//double currentInput;
	double previousTime;
	Timer teleopTimer;

	
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
	
	//Gyro

	ADXRS450_Gyro robotGyro;
	boolean calGyro; //true = gyro will calibrate
	
	//Drivetrain
	//drive motor controllers
	VictorSP leftDrive1, leftDrive2, rightDrive1, rightDrive2;
	//encoders
	Encoder leftDriveEncoder, rightDriveEncoder;
	//pneumatics (shifters)
	Solenoid driveShifter;
	boolean shifterLowGear, shifterButtonPrev;
		
	//Drive Function
	RobotDrive drivetrain;
	
	//XBoxControllers: driver: driving, operator: functions (like intake and shooter)
	XboxController driverController, operatorController;

	//Gear Manipulation
	//Pincher
	DoubleSolenoid pincher;
	boolean pincherClosed; //true = pinching
	boolean pincherButtonPrev; //state of the button from last iteration
	//Rotator, moves gear up/down
	Solenoid gearRotator; 
	boolean gearRotatorDown; //true = rotator down
	boolean gearRotatorButtonPrev; //state of the button from last iteration
	
	//Ball Manipulation
	//Feeder and Intake
	VictorSP intakeBelt, feederBelt; //belt motor controllers
	double feederMaxSpeed; //user input constant to control the speed of feederBelt
	//human load gate
	Solenoid HLGate;
	boolean HLGateOpen; //true = human load gate open
	boolean HLGateButtonPrev; //state of the button from last iteration
	//dumper gate
	Solenoid dumperGate;
	boolean dumperGateOpen; //true = dumper gate open
	boolean dumperButtonPrev; //state of the button from last iteration

	//Shooter
	//controllers (Talons)
	CANTalon shooterMotor1,shooterMotor2,shooterMotor3;
	boolean shooterOn;
	boolean shooterButtonPrev;
	//constant for shooter speed
	double OLShooterValue = 0.57; //"open loop shooter value" //no longer FINAL
	//target value for shooter speed, and constants for FPID
	double ShooterTarget, ShooterF, ShooterP, ShooterI, ShooterD;
	int ShooterIZone;
	//timer for how long error is below threshold
	Timer errorTimer;
	//threshold for shooter speed error - the feeder will run when under this error for a set time
	double shooterErrorThreshold = 20.0;
	//double shooterErrorThreshold; //check robotinit for value
	//the amount of time the shooter speed must be within the error threshold to be considered stable
	double shooterTimeDelay; //check robotinit for value
	
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
	int gearAutonCase = 0;
	double reachedTargetTime = 999;

	
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
		
		//camera
		CameraServer.getInstance().startAutomaticCapture();
		
		//Shooter
		//threshold for shooter speed error - the feeder will run when under this error for a set time
		shooterErrorThreshold = 20.0;
		//the amount of time the shooter speed must be within the error threshold to be considered stable
		shooterTimeDelay = 0.05;
		//put shooter constant data
		SmartDashboard.putNumber("Shooter constant", OLShooterValue);
		//get user input for constant, assign to OLShooterValue
		//puts a boolean (which becomes checkbox) on SmartDashboard
		SmartDashboard.putBoolean("Shooter PID on", true);
		//put error threshold for shooter speed to check to run feeder
		SmartDashboard.putNumber("Shooter Speed Error Threshold", shooterErrorThreshold);
		//put shooter time delay on SDB
		SmartDashboard.putNumber("Shooter Time Delay", shooterTimeDelay);
		
		
		//Assign Chooser for Autonomous programs
		autoChooser = new SendableChooser<String>();
		//autoChooser = new SendableChooser();
				
		//Assign Chooser for Teleop Drive Mode
		driveChooser = new SendableChooser<String>();
		//driveChooser = new SendableChooser();
		
		//Put Autonomous Chooser
		autoChooser.addDefault("Drive Forward", defaultAuto);
		autoChooser.addObject("Center Gear", centerGearAuto);
		autoChooser.addObject("Right Gear", rightGearAuto);
		autoChooser.addObject("Left Gear", leftGearAuto);
		autoChooser.addObject("Gyro Test Auto", gyroAuto);
		SmartDashboard.putData("Auto Choices", autoChooser);

		//Put Teleop Drive Mode Chooser
		driveChooser.addDefault("GTA Drive", GTAMode);
		driveChooser.addObject("Tank Drive", tankMode); 
		driveChooser.addObject("Arcade Drive", arcadeMode);
		SmartDashboard.putData("Drive Choices", driveChooser);

		//Autonomous mode timer
		autoTimer = new Timer();
		
		//Teleop mode timer
		teleopTimer = new Timer();
		
		//Assign Compressor
		//compressor = new Compressor();
		
		//Assign Gyro
		robotGyro = new ADXRS450_Gyro();
		SmartDashboard.putBoolean("Gyro Calibrate", true);
		//Drivetrain
		//Assign drive motor controllers
		leftDrive1 = new VictorSP(0);
		leftDrive2 = new VictorSP(1);
		rightDrive1 = new VictorSP(2);
		rightDrive2 = new VictorSP(3);
		
		//Assign drivetrain shifters
		driveShifter = new Solenoid(0);
		shifterLowGear = false; //false means high gear
		shifterButtonPrev = false;
		
		//Assign drivetrain encoders
		leftDriveEncoder = new Encoder(0, 1, false); //the boolean is to indicate if it is backward
		rightDriveEncoder = new Encoder(2, 3, true); //one should always be backward of the other
		leftDriveEncoder.setDistancePerPulse(0.0222);//one pulse is 1/45 of an inch
		rightDriveEncoder.setDistancePerPulse(0.0222);

		//Assign Drive Function
		drivetrain = new RobotDrive(leftDrive1, leftDrive2, rightDrive1, rightDrive2);
		
		//Assign XboxControllers
		driverController = new XboxController(0);
		operatorController = new XboxController(1);
		
		//Gear Manipulation
		//Assign Pincher
		pincher = new DoubleSolenoid(2,3);
		pincherClosed = true; //this makes setting up auto easier
		pincherButtonPrev = false;
		//Assign Rotator
		gearRotator = new Solenoid(4);
		gearRotatorDown = false;
		gearRotatorButtonPrev = false;
		
		//Ball Manipulation
		//Intake and Feeder
		//Assign belt motor controllers
		intakeBelt = new VictorSP(4); //ball intake
		feederBelt = new VictorSP(5); //shooter feed belt
		feederMaxSpeed = 1.0; //arbitrary starting value
		SmartDashboard.putNumber("Feeder Max Speed", feederMaxSpeed); //put feederMaxSpeed on SDB
		//Assign Human Load gate
		HLGate = new Solenoid(5);
		HLGateOpen = false;
		HLGateButtonPrev = false;
		//Assign Dumper Gate
		dumperGate = new Solenoid(6);
		dumperGateOpen = false;
		dumperButtonPrev = false;
		
		//Shooter
		//Assign Shooter motor controllers
		shooterMotor1 = new CANTalon(1); //verify IDs
		shooterMotor2 = new CANTalon(2); 
		shooterMotor3 = new CANTalon(3);
		//timer for how long error is below threshold
		errorTimer = new Timer();
		
		shooterMotor3.setFeedbackDevice(FeedbackDevice.QuadEncoder);
		//set other Talons to follow - verify correct numbers for everything
		shooterMotor2.changeControlMode(CANTalon.TalonControlMode.Follower);
		shooterMotor2.set(shooterMotor3.getDeviceID());
		shooterMotor1.changeControlMode(CANTalon.TalonControlMode.Follower);
		shooterMotor1.set(shooterMotor3.getDeviceID());
		
		//set nominal and peak voltage, 12V means full (but only here!)
		shooterMotor3.configNominalOutputVoltage(0.0, -0.0);
		shooterMotor3.configPeakOutputVoltage(12.0, -12.0);
		if (isPracticeRobot)
		{
			shooterMotor3.reverseSensor(true);
		}
		
		//FPID, TargetAND IZONE SETTINGS 
		
		// These aggressive PID settings are what we tried last.
		//Shooter Target setting for point-blank shot. Was 195 until recently
		ShooterTarget = 190;
		ShooterF = 2.9;
		ShooterP = 10;
		ShooterI = 0.04; //was 0.03
		ShooterD = 300; //was 260, 450, 400
		ShooterIZone = 5;
		
		//display target rpm, F, P, I, and D
		SmartDashboard.putNumber("Shooter Target", ShooterTarget);
		SmartDashboard.putNumber("Shooter F", ShooterF);
		SmartDashboard.putNumber("Shooter P", ShooterP);
		SmartDashboard.putNumber("Shooter I", ShooterI);
		SmartDashboard.putNumber("Shooter D", ShooterD);
		SmartDashboard.putNumber("Shooter I Zone", ShooterIZone);
		
		shooterMotor3.setProfile(0);//what the fuck is this
		shooterMotor3.setF(ShooterF);
		shooterMotor3.setP(ShooterP);
		shooterMotor3.setI(ShooterI);
		shooterMotor3.setD(ShooterD);
		shooterMotor3.setIZone(ShooterIZone);
		shooterButtonPrev = false;
		shooterOn = false;
		
		//Assign Hanging motor controllers
		hangingWinch = new VictorSP(6);
		hangingWinch.setInverted(true);
		hangFeedForward = false;
		hangButtonPrev = false;
		
		//set example mechanism and button statuses to false
		//exampleState = false;
		//exampleButtonPrev = false;
		
		//Assign the roboRIO's preferences, allowing us to load or save values,
		//such as strings, booleans, doubles, integers, and floats
		//These values are stored right on the roboRIO
		robotPrefs = Preferences.getInstance();
		
	}
	
	/**
	 * This function is called each time a new packet is received from the driver station
	 * (approximately every 20ms).
	 * Periodic code for all robot modes should go here.
	 */
	public void robotPeriodic()
	{
		//Put mechanism on/off values on SDB
		SmartDashboard.putBoolean("Drivetrain Low Gear", shifterLowGear);
		SmartDashboard.putBoolean("Pincher Closed", pincherClosed);
		SmartDashboard.putBoolean("Gear Down", gearRotatorDown);
		SmartDashboard.putBoolean("Human Gate Open", HLGateOpen);
		SmartDashboard.putBoolean("Dumper Open", dumperGateOpen);
		SmartDashboard.putBoolean("Shooter", shooterOn);
		SmartDashboard.putBoolean("Winch Feed Forward", hangFeedForward);
		//put on SDB whether it's set to practice robot
		SmartDashboard.putBoolean("Practice Robot Mode", isPracticeRobot);
		
		//checks if gyro calibration check box is on
		calGyro = SmartDashboard.getBoolean("Gyro Calibrate", true);

	}
	
	/**
	 * This code runs at the beginning of autonomous mode
	 */
	public void autonomousInit()
	{
		//get Autonomous selection
		autoSelected = autoChooser.getSelected();
		//print autonomous selection
		System.out.println("Auto selected: " + autoSelected);
		
		//zeros gyro. also ensures gyro finishes calibrating before continuing.
		robotGyro.reset();
		
		//reset encoders
		leftDriveEncoder.reset();
		rightDriveEncoder.reset();
		
		//starting the timer after the gyro ensures auto runs for correct length even if delayed
		autoTimer.reset();
		autoTimer.start();
	}

	/**
	 * This function is called periodically during autonomous
	 * The switch case will determine which autonomous mode runs based on the chooser
	 */
	public void autonomousPeriodic()
	{
		switch(autoSelected)
		{
		case centerGearAuto:
			gearAuto("Center");   
			break;
		case rightGearAuto:
			gearAuto("Right");
			break;
		case leftGearAuto:
			gearAuto("Left");
			break;
		case gyroAuto:
			gyroTestAuto();
			break;
		case defaultAuto:
			driveForwardAuto(80);
			break; //80 inches is clearly forward of center peg
		default:
			drivetrain.arcadeDrive(0.0, 0.0); //prevents watchdog error
			break;
		}
	}
	/**This just drives forward for a set number of seconds.
	* Never Tested!!!
	*/
	public void driveForwardAuto (double driveDistance)
	{
		final double gyroP = -0.195;
		double averageDistance;
		driveShifter.set(true);
		
		//if below code is uncommented, please comment this out
		averageDistance = (leftDriveEncoder.getDistance() + rightDriveEncoder.getDistance()) / 2;
		System.out.println(averageDistance);
		//error handling for encoder - if one encoder is totally dead, autonomous won't totally fail
		//TEST ME BEFORE UNCOMMENTING
		/*
		if (leftDriveEncoder.getDistance() == 0 && rightDriveEncoder.getDistance() > 6)
		{
			averageDistance = rightDriveEncoder.getDistance();
		}
		else if (rightDriveEncoder.getDistance() == 0 && leftDriveEncoder.getDistance > 6)
		{
			averageDistance = leftDriveEncoder.getDistance();
		}
		else
		{
			averageDistance = (leftDriveEncoder.getDistance() + rightDriveEncoder.getDistance()) / 2;
		}
		*/
		switch (gearAutonCase) //yes I'm bad and should make a new variable
		{
		case 0:
			if (averageDistance < driveDistance)
			{
				drivetrain.arcadeDrive(-0.7, robotGyro.getAngle() * gyroP);
			}
			else
			{
				gearAutonCase++;
			}
			break;
		case 1:
		default:
			drivetrain.arcadeDrive(0, 0);
			break;
		}
	}
	
	
	/**This scores a gear on a peg directly in front of the robot.
	*/
	
	public void gearAuto(String sideOfField)
	{
		final double gyroP = -0.195;
		final double targetDistance1 = 69;
		final double targetAngleRight = -60;
		final double targetAngleLeft = 60;
		final double targetDistanceTwo = 72; //was 74
		double targetDistance2;
		double averageDistance;
		
		//the side autos drive forward for a slightly shorter distance
		//the adjustment is made here so it can be tweaked easily
		if (sideOfField == "Left" || sideOfField == "Right")
		{
			targetDistance2 = targetDistanceTwo - 4; //was 6, adjusted based on adjusting two
		}
		else
		{
			targetDistance2 = targetDistanceTwo;
		}
		
		driveShifter.set(true); // low gear
		
		//if below code is uncommented, please comment this out
		averageDistance = (leftDriveEncoder.getDistance() + rightDriveEncoder.getDistance()) / 2;
		
		//error handling for encoder - if one encoder is totally dead, autonomous won't totally fail
		//TEST ME BEFORE UNCOMMENTING
		/*
		if (leftDriveEncoder.getDistance() == 0 && rightDriveEncoder.getDistance() > 6)
		{
			averageDistance = rightDriveEncoder.getDistance();
		}
		else if (rightDriveEncoder.getDistance() == 0 && leftDriveEncoder.getDistance > 6)
		{
			averageDistance = leftDriveEncoder.getDistance();
		}
		else
		{
			averageDistance = (leftDriveEncoder.getDistance() + rightDriveEncoder.getDistance()) / 2;
		}
		*/
		
		switch(gearAutonCase)
		{
		case 0: //check which direction you're going
			if (sideOfField == "Left" || sideOfField == "Right")
			{
				gearAutonCase++; //case 1 is either side case
			}
			else
			{
				gearAutonCase += 10; //case 2 is center gear auto case
				//the case 1 step drives the left / right cases forward
				//and turns them until they are in the center gear position
			}
			break;
		case 1: //for left-right auto, go straight out to rotation point
				//this is an area that can be sped up but play with setpoints if you do
			if (averageDistance < targetDistance1)
			{
				drivetrain.arcadeDrive(-0.7, robotGyro.getAngle() * gyroP);
			}
			else if (averageDistance > targetDistance1 + 4)
			{
				drivetrain.arcadeDrive(0.7, robotGyro.getAngle() * gyroP);
			}
			else
			{
				gearAutonCase++;
			}
			break;
		case 2: //spin a certain angle
		//i am pretty sure i could make a variable changing the sign of getAngle to make this work as 1 call 
		//but that would require testing time we just don't have at the moment
			if (sideOfField == "Right") //if sideOfField == "Right"
			{
				System.out.println(robotGyro.getAngle());
				if (robotGyro.getAngle() > targetAngleRight)
				{
					drivetrain.arcadeDrive(0, -0.75);
				}
				else if (robotGyro.getAngle() < targetAngleRight - 1)
				{
					drivetrain.arcadeDrive(0, +0.75); //consider tuning this down a little
				}
				else
				{
					System.out.println("Got to this part");
					robotGyro.reset();
					leftDriveEncoder.reset();
					rightDriveEncoder.reset();
					drivetrain.arcadeDrive(0, 0);
					gearAutonCase = 10; 
				}
			}
			if (sideOfField == "Left")
			{
				System.out.println(robotGyro.getAngle());
				if (robotGyro.getAngle() < targetAngleLeft)
				{
					drivetrain.arcadeDrive(0, 0.75);
				}
				else if (robotGyro.getAngle() > targetAngleLeft + 1)
				{
					drivetrain.arcadeDrive(0, -0.75); //consider tuning this down a little
				}
				else
				{
					System.out.println("Got to this part");
					robotGyro.reset(); //ensures we are starting at zero for straight line auto
					leftDriveEncoder.reset();
					rightDriveEncoder.reset();
					drivetrain.arcadeDrive(0, 0);
					gearAutonCase = 10; 
				}
			}
			break;
		case 10: //drive to target distance straight forward
			if (averageDistance < targetDistance2)
			{
				drivetrain.arcadeDrive(-0.7, robotGyro.getAngle() * gyroP);
			}
			else if (averageDistance > targetDistance2 + 4)
			{
				drivetrain.arcadeDrive(0.7, robotGyro.getAngle() * gyroP);
			}
			else
			{
				drivetrain.arcadeDrive(0, 0);
				reachedTargetTime = autoTimer.get();
				gearAutonCase++; //move on to the next round!
			}
			break;
		case 11: //after waiting 0.5 second, drop the thing, after 1, drive back, after 2.5, advance
			if (autoTimer.get() > reachedTargetTime + 0.5)
			{
				pincher.set(DoubleSolenoid.Value.kReverse);
			}
			if (autoTimer.get() > reachedTargetTime + 1)
			{
				drivetrain.arcadeDrive(0.7, robotGyro.getAngle() * gyroP);
			}
			if (autoTimer.get() > reachedTargetTime + 2.5)
			{
				drivetrain.arcadeDrive(0, 0);
				gearAutonCase++;
			}
			break;
		case 12: //if you get time, add a spin move to reach center field
		default:
			drivetrain.arcadeDrive(0, 0);
			break;
		}
	}
	
	/**
	 * This method checks the angle on the gyro repeatedly.
	 * It uses a delay, which I don't like and shouldn't be done in real code
	 */
	public void gyroTestAuto()
	{
		//turn off drivetrain
		drivetrain.arcadeDrive(0.0, 0.0);
		//check gyro
		System.out.println(robotGyro.getAngle());
		System.out.println(leftDriveEncoder.get());
		System.out.println(rightDriveEncoder.get());
		//wait .1s
		Timer.delay(0.1);
	}
	
	/**
	 * This function is called once at the beginning of operator control
	 */
	public void teleopInit()
	{
		//get drive mode selection (tank, arcade, GTA)
		//driveMode = (String) driveChooser.getSelected();
		//print drive mode selection
		//System.out.println("Drive mode selected: " + driveMode);
		teleopTimer.reset();
		teleopTimer.start();
		
		previousTime = 0;
		previousInput = 0;
	}

	/**
	 * This function is called periodically during operator control
	 */
	public void teleopPeriodic()
	{
		//DRIVER CONTROLS
		//Value for the GTA Mode arcade function and SmartDashboard data
		double combinedTriggerValue;
		//for the "go" axis of arcade drive and axes of tank drive. this lets us square the values properly
		double arcadeLeftStick;
		double tankLeftStick;
		double tankRightStick;
		
		//Assign drive mode selection to driveMode
		driveMode = (String)driveChooser.getSelected();
		
		//Start drive mode
		switch(driveMode)
		{
		case arcadeMode:
			//square the value of the left axis. if negative, it must be multiplied by -1 after squaring
			if (driverController.getRawAxis(1) >= 0)
			{
				arcadeLeftStick = Math.pow(driverController.getRawAxis(1), 2);
			}
			else
			{
				arcadeLeftStick = (-1 * Math.pow(driverController.getRawAxis(1), 2));
			}
			
			//start arcade mode
			drivetrain.arcadeDrive(arcadeLeftStick, driverController.getRawAxis(4));
			
			//since we aren't in GTA mode, the left bumper controls shifting
			shifterControl(driverController.getRawButton(5));
			
			break;
			
		case tankMode:
			//square the axis value. if negative, multiply by -1 after squaring
			if (driverController.getRawAxis(1) >= 0)
			{
				tankLeftStick = Math.pow(driverController.getRawAxis(1), 2);
			}
			else
			{
				tankLeftStick = (-1 * Math.pow(driverController.getRawAxis(1), 2));
			}
			//same for right axis
			if (driverController.getRawAxis(5) >= 0)
			{
				tankRightStick = Math.pow(driverController.getRawAxis(5), 2);
			}
			else
			{
				tankRightStick = (-1 * Math.pow(driverController.getRawAxis(5), 2));
			}
			
			//do tank mode
			drivetrain.tankDrive(tankLeftStick, tankRightStick);
			
			//the old, simple tank mode:
			//drivetrain.tankDrive(driverController, 1, driverController, 5);
			
			//since we aren't in GTA mode, the left bumper controls shifting
			shifterControl(driverController.getRawButton(5));
			break;
			
		case GTAMode:
		default: //we needed a default case to prevent watchdog errors if smartdashboard didn't work
			combinedTriggerValue = (-1 * Math.pow(driverController.getRawAxis(2), 2) + Math.pow(driverController.getRawAxis(3), 2));

			if(driveShifter.get() == true)
			{
				drivetrain.arcadeDrive(combinedTriggerValue, driverController.getRawAxis(0));
				//if we are in GTA mode, shifting is assigned to a face button instead
				shifterControl(driverController.getAButton());
				//display combinedTriggerValue
				//SmartDashboard.putNumber("GTADriveValue", combinedTriggerValue);
			}
			else
			{
				drivetrain.arcadeDrive(rateLimiter(combinedTriggerValue), driverController.getRawAxis(0));
			}
			
			break;
		}
		
		//OPERATOR CONTROLS
		//Pincher: toggle  with B (DoubleSolenoid.Value.kForward = closed, ...kReverse = open)
		pincherControl(operatorController.getBButton());

		//Gear Rotator: toggle with A (on/down)/(DEFAULT: off/up)
		gearRotatorControl(operatorController.getAButton());
		
		//Ball intake and feed combined function, left trigger and left joystick y axis
		intakeAndFeedBalls(operatorController.getRawAxis(2), operatorController.getRawAxis(1));

		//Human Load Gate: toggle with Y (on/open)/(DEFAULT: off/closed)
		HLGateControl(operatorController.getYButton());
		
		//Dumper Gate: toggle with right bumper (6) (on/open/shoot)/(DEFAULT: off/closed/no shoot)
		dumperGateControl(operatorController.getRawButton(6));

		//Shooter: spin up/down toggle with left bumper
		shooterControl(operatorController.getRawButton(5), SmartDashboard.getBoolean("Shooter PID on", false)); //set to true for PID

		//Hanging: right trigger passes for throttle value, X button toggles feed-forward on and off
		//TEMPORARILY CHANGED TO RIGHT STICK with 5 (3 is right button)
		hangingControl(operatorController.getRawAxis(3), operatorController.getXButton());
	}
	
	public void disabledInit()
	{
		//set all of the toggle states for everything to false
		//this prevents unexpected movement on re-enable
		//pincherClosed = false; //pincherClosed should not change actually
		gearRotatorDown = false;
		HLGateOpen = false;
		dumperGateOpen = false;
		shooterOn = false;
		//shifterLowGear = false;
		//hangFeedForward = false; //also not sure here
		
		//in case the robot was in test mode, shooter motor 1 and 2 may not be in follower mode anymore
		//to be safe, they are set to follower mode every time the robot is disabled
		shooterMotor2.changeControlMode(CANTalon.TalonControlMode.Follower);
		shooterMotor2.set(shooterMotor3.getDeviceID());
		shooterMotor1.changeControlMode(CANTalon.TalonControlMode.Follower);
		shooterMotor1.set(shooterMotor3.getDeviceID());
		
		//set auton variables back to default
		//clean up later
		reachedTargetTime = 999;
		gearAutonCase = 0;
	}
	
	/**
	 * This function is called periodically during disabled mode
	 */
	public void disabledPeriodic()
	{
		//if "Gyro Calibrate" is checked
		//calibrate the gyro
		if (calGyro)
		{
			robotGyro.calibrate();
		}
		//reachedTargetTime = 999; // bug fix for auto ask chris sorry
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
			pincherClosed = !pincherClosed;
		}
		//now that check is complete, store value of pincherButton for next iteration of loop
		pincherButtonPrev = pincherButton;

		if (pincherClosed)
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
			gearRotatorDown = !gearRotatorDown;
		}
		//now that check is complete, store value of gearRotatorButton for next iteration of loop
		gearRotatorButtonPrev = gearRotatorButton;

		if (gearRotatorDown)
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
	* This function is a copy of both the intake and feed functions, put together.
	* This prevents conflicts between the two.
	* When you get time later, overload this function with a one-constructor version
	* intakeSpeed and feedSpeed are values based on the joystick input
	* for intake and feeding respectively
	*/
	public void intakeAndFeedBalls(double intakeSpeed, double feedSpeed)
	{
		//get user input for Feeder Max Speed
		feederMaxSpeed = SmartDashboard.getNumber("Feeder Max Speed", feederMaxSpeed);	
		
		//get error threshold for shooter speed to check to run feeder
		shooterErrorThreshold = SmartDashboard.getNumber("Shooter Speed Error Threshold", shooterErrorThreshold);
		
		//eliminates deadband caused by cheapo xbox controllers
		if (intakeSpeed > -0.2 && intakeSpeed < 0.2) 
		{
			intakeSpeed = 0;
		}
		if (feedSpeed > -0.2 && feedSpeed < 0.2)
		{
			feedSpeed = 0;
		}
		
		//run the feeder and intake
		//if PID is on and shooter is on
		if (SmartDashboard.getBoolean("Shooter PID on", false) && shooterOn)
		{
			//if shooter is stable at correct speed
			if (mechanismStable(shooterMotor3.getClosedLoopError() < shooterErrorThreshold
					&& shooterMotor3.getClosedLoopError() > (shooterErrorThreshold*-1)))
			{
				//run the feeder
				feederBelt.set((-1*(intakeSpeed + feedSpeed))*feederMaxSpeed);
				//run the intake
				intakeBelt.set(intakeSpeed + (-1 * feedSpeed));
			}
			else
			{
				//don't run feeder
				feederBelt.set(0);
				//don't run the intake
				intakeBelt.set(0);
			}
		}
		//if not PID or shooter not on
		else
		{
			//run the feeder
			feederBelt.set((-1*(intakeSpeed + feedSpeed))*feederMaxSpeed);
			//run the intake
			intakeBelt.set(intakeSpeed + (-1 * feedSpeed));
		}

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
			HLGateOpen = !HLGateOpen;
		}
		//now that check is complete, store value of HLGateButton for next iteration of loop
		HLGateButtonPrev = HLGateButton;

		if (HLGateOpen)
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
			dumperGateOpen = !dumperGateOpen;
		}
		//now that check is complete, store value of dumperButton for next iteration of loop
		dumperButtonPrev = dumperButton;

		if (dumperGateOpen)
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
	 * This function toggles the drivetrain shifters using a button
	 * We will probably also call this in autonomous
	 * @param shifterButton
	 */
	
	public void shifterControl(boolean shifterButton)
	{
		//if button is pressed and the state changed (button isn't being held)
		if (shifterButton && shifterButton != shifterButtonPrev) 
		{
			//toggle the shifter state (false means high gear!)
			shifterLowGear = !shifterLowGear;
		}
		//now that the check is complete, store button value for next loop
		shifterButtonPrev = shifterButton;
		
		if (shifterLowGear)
		{
			//set to low gear
			driveShifter.set(true);
		} 
		else 
		{
			//set to high gear
			driveShifter.set(false);
		}
	}
	
	/**
	 * This function handles the shooter motors on the 2017 robot.
	 * @param shooterButton, isPID
	 */
	public void shooterControl(boolean shooterButton, boolean isPID)
	{
		
		//Graph Signal and Error on SmartDashboard. Display Speed as number
		SmartDashboard.putNumber("Shooter Signal", shooterMotor3.getOutputVoltage() / shooterMotor3.getBusVoltage());
		SmartDashboard.putNumber("Shooter Error", shooterMotor3.getClosedLoopError());
		SmartDashboard.putNumber("Shooter Speed", shooterMotor3.getSpeed());
		//move back if needed to inside PID code
		
		//get user input for constant, assign to OLShooterValue
		OLShooterValue = SmartDashboard.getNumber("Shooter constant", OLShooterValue);
		
		//if a new button press has started, turn shooter on/off
		//toggle shooter
		if(shooterButton && shooterButton != shooterButtonPrev)
		{
			shooterOn = !shooterOn;
		}
		
		shooterButtonPrev = shooterButton;
		
		if (shooterOn) //if the shooter has toggled on
		{
			//SmartDashboard.putBoolean("Shooter On", true); //indicate on dash that shooter is on //this is now in robotPeriodic
			if (isPID) //if the PID check box on smartdashboard is on
			{
				//get latest constants from SmartDashboard
				ShooterTarget = SmartDashboard.getNumber("Shooter Target", ShooterTarget);
				ShooterF = SmartDashboard.getNumber("Shooter F", ShooterF);
				ShooterP = SmartDashboard.getNumber("Shooter P", ShooterP);
				ShooterI = SmartDashboard.getNumber("Shooter I", ShooterI);
				ShooterD = SmartDashboard.getNumber("Shooter D", ShooterD);
				ShooterIZone = (int) SmartDashboard.getNumber("Shooter I Zone", ShooterIZone);
				
				//double ShooterTarget = 4000.0; //now a global variable
				shooterMotor3.changeControlMode(TalonControlMode.Speed);
				//encoder is 12 CPR (counts per revolution)
				//speed setpoint is ticks per 10ms

				//set PID loop to values from smart dashboard
				shooterMotor3.set(ShooterTarget);
				shooterMotor3.setF(ShooterF);
				shooterMotor3.setP(ShooterP);
				shooterMotor3.setI(ShooterI);
				shooterMotor3.setD(ShooterD);
				shooterMotor3.setIZone(ShooterIZone);
				
			}
			else //if PID is unchecked
			{
				//change talon to open-loop mode, set a value
				shooterMotor3.changeControlMode(TalonControlMode.PercentVbus);
				shooterMotor3.set(OLShooterValue); //this value is from SmartDashboard
			}
		}
		else
		{
			//change talon to open-loop mode, set to off
			//SmartDashboard.putBoolean("Shooter On", false); //like above, this is now in robotPeriodic
			shooterMotor3.changeControlMode(TalonControlMode.PercentVbus);
			shooterMotor3.set(0.0);
		}
	}
	
	/**This method will take a boolean (that is, if the absolute value error of the shooter
	 * is under a certain threshold), and if true (that is, the error is under that threshold),
	 * it will start a timer. If the timer reaches a certain amount of time (indicating the shooter
	 * is stable at the desired speed), the method will return true. If the timer has not reached
	 * that time, the method will return false. If the input boolean is false (that is, the error is
	 * over the threshold), the timer will stop, then reset, and the method will return false.
	 * 
	 * the return value of the method will be used to determine whether the shooter feed will run.
	 * @param isLowError
	 * @return
	 */
	public boolean mechanismStable(boolean isLowError)
	{
		//get time delay from SDB
		SmartDashboard.getNumber("Shooter Time Delay", shooterTimeDelay);
		
		//if the error is below threshold
		if (isLowError)
		{
			//if the timer has not started (it will be 0)
			if (errorTimer.get() == 0.0)
			{
				errorTimer.start();
			}
			//if timer is under user input shooterTimeDelay
			if (errorTimer.get() > shooterTimeDelay)
			{
				//run feeder
				return true;
			}
			//if the error is above threshold and timer is not < 0.25s
			else
			{
				//don't run feeder
				return false;
			}
		}
		//if the error is not below threshold
		else
		{
			//stop the timer
			errorTimer.stop();
			//reset the timer
			errorTimer.reset();
			//run the feeder
			return false;
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
		final double hangFFValue = 0.3; //change this to change how much FF to apply
		
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
	
	
	/** Test mode is used in order to verify motors are working properly and spinning in correct direction.
	* Do not use for anything other than debugging!!
	*/
	
	//Currently Test mode is only used to make sure all 3 shooter motors are independently working
	
	public void testInit() {
		//change motors 1 and 2 back to percent voltage mode
		shooterMotor1.changeControlMode(TalonControlMode.PercentVbus);
		shooterMotor2.changeControlMode(TalonControlMode.PercentVbus);
	}
	
	public void testPeriodic() {
		if (operatorController.getAButton())
		{
			shooterMotor1.set(1.0);
		}
		else
			shooterMotor1.set(0);
		if (operatorController.getBButton())
		{
			shooterMotor2.set(1.0);
		}
		else
		{
			shooterMotor2.set(0);
		}
		if (operatorController.getXButton())
		{
			shooterMotor3.set(1.0);
		}
		else
		{
			shooterMotor3.set(0);
		}
	}
	/**
	 * This function will limit how fast the robot can travel.  We'll probably have to change this and how it works
	 * @param currentInput
	 * @return the limit
	 */
	public double rateLimiter(double currentInput) {
		double speedRate;
		double currentTime = teleopTimer.get();
		double rateLimit = 2.0;
		
		speedRate = (currentInput - previousInput) / (currentTime - previousTime);
		
		if(speedRate > rateLimit) {
			currentInput = previousInput + rateLimit * (currentTime - previousTime);
		}
		else if (speedRate < (-1) * rateLimit) {
			currentInput = previousInput - rateLimit * (currentTime - previousTime);
		}
		return currentInput;
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