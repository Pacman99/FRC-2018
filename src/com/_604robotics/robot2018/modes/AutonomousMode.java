package com._604robotics.robot2018.modes;

import com._604robotics.robot2018.Robot2018;
import com._604robotics.robot2018.constants.Calibration;
import com._604robotics.robot2018.macros.ArcadeTimedDriveMacro;
import com._604robotics.robot2018.modules.Arm;
import com._604robotics.robot2018.modules.Clamp;
import com._604robotics.robot2018.modules.Drive;
import com._604robotics.robot2018.modules.Elevator;
import com._604robotics.robot2018.modules.Intake;
import com._604robotics.robotnik.Coordinator;
import com._604robotics.robotnik.Logger;
import com._604robotics.robotnik.prefabs.coordinators.SimultaneousCoordinator;
import com._604robotics.robotnik.prefabs.coordinators.SleepCoordinator;
import com._604robotics.robotnik.prefabs.coordinators.StatefulCoordinator;
import com._604robotics.robotnik.prefabs.coordinators.SwitchCoordinator;
import com._604robotics.robotnik.prefabs.flow.Pulse;
import com._604robotics.robotnik.prefabs.flow.SmartTimer;
import com._604robotics.robotnik.utils.AutonMovement;
import com._604robotics.robotnik.utils.annotations.Unreal;

import edu.wpi.first.wpilibj.PIDController;
import edu.wpi.first.wpilibj.PIDOutput;
import edu.wpi.first.wpilibj.PIDSource;
import edu.wpi.first.wpilibj.PIDSourceType;

public class AutonomousMode extends Coordinator {
    private final Robot2018 robot;

    private final Coordinator rotateLeftStateMacro;
    private final Coordinator rotateRightStateMacro;
    private final Coordinator forwardStateMacro;
    private final Coordinator forwardSwitchMacro;
    private final Coordinator backwardStateMacro;
    private final Coordinator kinematicFallback;

    private Coordinator selectedModeMacro;

    public AutonomousMode (Robot2018 robot) {
        this.robot = robot;

        rotateLeftStateMacro = new ArcadePIDStateMacro(Calibration.DRIVE_MOVE_STILL_TARGET,
                Calibration.DRIVE_ROTATE_LEFT_TARGET);
        rotateRightStateMacro = new ArcadePIDStateMacro(Calibration.DRIVE_MOVE_STILL_TARGET,
                Calibration.DRIVE_ROTATE_RIGHT_TARGET);
        forwardStateMacro = new ArcadePIDStateMacro(Calibration.DRIVE_MOVE_FORWARD_TARGET,
                Calibration.DRIVE_ROTATE_STILL_TARGET);
        forwardSwitchMacro = new ArcadePIDStateMacro(Calibration.DRIVE_MOVE_FORWARD_SWITCH_INCHES, 0);
        backwardStateMacro = new ArcadePIDStateMacro(Calibration.DRIVE_MOVE_BACKWARD_TARGET,
                Calibration.DRIVE_ROTATE_STILL_TARGET);
        kinematicFallback =new ArcadeTimedDriveMacro(robot) {{
            
        }

        @Override
        protected double getMovePower() {
            return Calibration.DRIVE_MOVE_PID_MAX;
        }

        @Override
        protected double getRotatePower() {
            return 0;
        }

        @Override
        protected double getTime() {
        	// TODO: change to a calibration value
            return 5;
        }};
    }

    @Override
    public void begin () {
        switch (robot.dashboard.autonMode.get()) {
	        case CENTER_SWITCH:
	        	selectedModeMacro = new CenterSwitchMacro();
	        	break;
	        case LEFT_SCALE:
	        	selectedModeMacro = new LeftScaleMacro();
	        	break;
	        case RIGHT_SCALE:
	        	selectedModeMacro = new RightScaleMacro();
	        	break;
            case ROTATE_LEFT_TEST:
                selectedModeMacro = rotateLeftStateMacro;
                break;
            case ROTATE_RIGHT_TEST:
                selectedModeMacro = rotateRightStateMacro;
                break;
            case FORWARD_6:
                selectedModeMacro = forwardStateMacro;
                break;
            case BACKWARD_6:
                selectedModeMacro = backwardStateMacro;
                break;
            case DEMO_NEW_AUTON:
                selectedModeMacro = new DemoStateMacro();
                break;
            case FORWARD_SWITCH:
                selectedModeMacro = forwardSwitchMacro;
                break;
            case CENTER_SWITCH_LEFT:
                selectedModeMacro = new CenterMacroLeft();
                break;
            case CENTER_SWITCH_RIGHT:
                selectedModeMacro = new CenterMacroRight();
                break;
            case SWERVE_SCALE_OPPOSITE_LEFT:
                selectedModeMacro = new SwerveScaleOppositeMacroLeft();
                break;
            case SWITCH_FORWARD:
                selectedModeMacro = new SwitchForwardMacro();
                break;
            case SCALE_BACKWARD:
                selectedModeMacro = new ScaleBackwardMacro();
                break;
            case SCALE_BACKWARD_2:
                selectedModeMacro = new ScaleBackwardMacro2();
                break;
            case NEW_SCALE_BACKWARD:
                selectedModeMacro = new NewScaleBackwardMacroLeft();
                break;
            case SCALE_OPPOSITE:
                selectedModeMacro = new ScaleOppositeMacroLeft();
                break;
            case PERSISTENCE_TEST:
            	selectedModeMacro = new SimultaneousMacro();
            	break;
            case BALANCED_LEFT_TURN_TEST:
            	selectedModeMacro = new BalancedLeftTurnMacro();
            	break;
            case SWEPT_LEFT_TURN_TEST:
            	selectedModeMacro = new SweptLeftTurnMacro();
            	break;
            case BALANCED_SWEPT_LEFT_TURN_TEST:
            	selectedModeMacro = new BalancedSweptLeftTurnMacro();
            	break;
            case BALANCED_RIGHT_TURN_TEST:
            	selectedModeMacro = new BalancedRightTurnMacro();
            	break;
            case SWEPT_RIGHT_TURN_TEST:
            	selectedModeMacro = new SweptRightTurnMacro();
            	break;
            case BALANCED_SWEPT_RIGHT_TURN_TEST:
            	selectedModeMacro = new BalancedSweptRightTurnMacro();
            	break;
            default:
                selectedModeMacro = null;
                break;
        }

        if (selectedModeMacro != null) {
            selectedModeMacro.start();
        }
    }

    @Override
    public boolean run () {
        if (selectedModeMacro == null) {
            return false;
        }

        return selectedModeMacro.execute();
    }

    @Override
    public void end () {
        if (selectedModeMacro != null) {
            selectedModeMacro.stop();
        }
    }
    
    protected final class ClampExtend extends Coordinator {
    	private Clamp.HoldExtend autonClampExtend;
    	private boolean sent;
    	
    	public ClampExtend() {
    		autonClampExtend = robot.clamp.new HoldExtend();
    		sent = false;
    	}
    	
    	@Override
    	public void begin() {
    		autonClampExtend.activate();
    		sent = false;
    	}
    	
    	@Override
    	public boolean run() {
    		if( sent ) {
    			return false;
    		} else {
    			sent = true;
        		autonClampExtend.activate();
        		return true;
    		}
    	}
    	
    	@Override
    	public void end() {
    		// Do nothing
    	}
    }
    
    protected final class ClampRetract extends Coordinator {
    	private Clamp.HoldRetract autonClampRetract;
    	private boolean sent;
    	
    	public ClampRetract() {
    		autonClampRetract = robot.clamp.new HoldRetract();
    		sent = false;
    	}
    	
    	@Override
    	public void begin() {
    		autonClampRetract.activate();
    		sent = false;
    	}
    	
    	@Override
    	public boolean run() {
    		if( sent ) {
    			return false;
    		} else {
    			sent = true;
        		autonClampRetract.activate();
        		return true;
    		}
    	}
    	
    	@Override
    	public void end() {
    		// Do nothing
    	}
    }
    
    protected final class ArmSetPersistent extends Coordinator {
    	private Arm.SetPersistent autonArmSetPersistent;
    	private double setpoint;
    	private boolean sent;
    	
    	public ArmSetPersistent(double clicks) {
    		setpoint = clicks;
    		autonArmSetPersistent = robot.arm.new SetPersistent(setpoint);
    		sent = false;
    	}
    	
    	@Override
    	public void begin() {
    		autonArmSetPersistent.activate();
    		sent = false;
    	}
    	
    	@Override
    	public boolean run() {
    		if( sent ) {
    			return false;
    		} else {
    			sent = true;
        		autonArmSetPersistent.activate();
        		return true;
    		}
    	}
    	
    	@Override
    	public void end() {
    		// Do nothing
    	}
    	
    }
    
    protected final class ElevatorSetPersistent extends Coordinator {
    	private Elevator.SetPersistent autonElevatorSetPersistent;
    	private double setpoint;
    	private boolean sent;
    	
    	public ElevatorSetPersistent(double clicks) {
    		setpoint = clicks;
    		autonElevatorSetPersistent = robot.elevator.new SetPersistent(setpoint);
    		sent = false;
    	}
    	
    	@Override
    	public void begin() {
    		autonElevatorSetPersistent.activate();
    		sent = false;
    	}
    	
    	@Override
    	public boolean run() {
    		if( sent ) {
    			return false;
    		} else {
    			sent = true;
        		autonElevatorSetPersistent.activate();
        		return true;
    		}
    	}
    	
    	@Override
    	public void end() {
    		// Do nothing
    	}
    	
    }
    
    protected final class ElevatorMove extends Coordinator {
    	private Elevator.Setpoint autonElevator;
    	private SmartTimer timeElapsed = new SmartTimer();
    	private double elevatorPosition;
    	private double time;
    	
    	public ElevatorMove(double elevatorPos, double seconds) {
    		elevatorPosition = elevatorPos;
    		time = seconds;
    		autonElevator = robot.elevator.new Setpoint(elevatorPosition);
    	}
    	
    	 @Override
         public void begin() {
             autonElevator.activate();
             timeElapsed.start();
         }
         
         @Override
         public boolean run() {
             autonElevator.activate();
             return !timeElapsed.hasReachedTime(time);
         }
         
         @Override
         public void end() {
             timeElapsed.stopAndReset();
         }
    }
    
    protected final class ArmMove extends Coordinator {
        private Arm.Setpoint autonArm;
        private SmartTimer timeElapsed = new SmartTimer();
        private double armPosition;
        private double time;
        
        public ArmMove(double armPos, double seconds) {
            armPosition = armPos;
            time = seconds;
            autonArm = robot.arm.new Setpoint(armPosition);
        }
        
        @Override
        public void begin() {
            autonArm.activate();
            timeElapsed.start();
        }
        
        @Override
        public boolean run() {
            autonArm.activate();
            System.out.println("Reachout!");
            return !timeElapsed.hasReachedTime(time);
        }
        
        @Override
        public void end() {
            timeElapsed.stopAndReset();
        }
    }

    protected final class IntakeMove extends Coordinator {
    	private SmartTimer timeElapsed;
    	private Intake.Run autonIntake;
    	private double power;
    	private double time;
    	
    	public IntakeMove(double pow, double seconds) {
    		power = pow;
    		time = seconds;
    		timeElapsed = new SmartTimer();
    		autonIntake = robot.intake.new Run(power);
    	}
    	
    	@Override
    	public void begin() {
    		timeElapsed.start();
    		autonIntake.activate();
    	}
    	
    	@Override
    	public boolean run() {
    		autonIntake.activate();
    		return !timeElapsed.hasReachedTime(time);
    	}
    	
    	@Override
    	public void end() {
    		timeElapsed.stopAndReset();
    	}
    }
    
    protected final class ArcadePIDCoordinator extends Coordinator {
        private Logger arcadePIDLog=new Logger(ArcadePIDCoordinator.class);
        // Timer that is started to continue running PID for some time after equilibrium
        private SmartTimer timeElapsed = new SmartTimer();
        private Drive.ArcadeDrive arcadeDrive = robot.drive.new ArcadeDrive(false);
        private Pulse PIDTargetPulse=new Pulse();
        // PIDOutputs write to persistent Input that retain their values
        // This prevents jerky movement when PIDs don't run often enough
        // Rotation PIDOutput
        private PIDOutput rotateBot = new PIDOutput() {
            @Override
            public synchronized void pidWrite(double output) {
                arcadeDrive.rotatePower.set(output);
            }
        };
        // Move PIDOutput
        private PIDOutput moveBot = new PIDOutput() {
            @Override
            public synchronized void pidWrite(double output) {
                arcadeDrive.movePower.set(output);
            }
        };
        // Encoder rotation PIDSource
        private PIDSource encoderDiff = new PIDSource() {

            private PIDSourceType type;

            @Override
            public void setPIDSourceType(PIDSourceType pidSource) {type = pidSource;}

            @Override
            public PIDSourceType getPIDSourceType() {return type;}

            @Override
            public double pidGet() {
                return -robot.drive.rightClicks.get()+robot.drive.leftClicks.get();
            }
        };
        // Encoder moving PIDSource
        private PIDSource encoderAvg = new PIDSource() {

            private PIDSourceType type;

            @Override
            public void setPIDSourceType(PIDSourceType pidSource) {type = pidSource;}

            @Override
            public PIDSourceType getPIDSourceType() {return type;}

            @Override
            public double pidGet() {
                return (robot.drive.rightClicks.get()+robot.drive.leftClicks.get())/2;
            }

        };
        // Declaration of PIDControllers
        // Avoid initialization here because parameters require tweaking as well
        private PIDController rotController;
        private PIDController moveController;
        private double moveSetpoint;
        private double rotSetpoint;

        public ArcadePIDCoordinator(double moveSetpoint, double rotSetpoint) {
            super();
            this.moveSetpoint = moveSetpoint;
            this.rotSetpoint = rotSetpoint;
        }

        public double getMoveError() {
            return moveController.getError();
        }

        public double getRotError() {
            return rotController.getError();
        }

        @Override
        protected void begin() {
            // Activate arcadeDrive and reset encoder and gyro
            arcadeDrive.activate();
            robot.drive.resetSensors();
            // Set up PIDSource details
            encoderDiff.setPIDSourceType(PIDSourceType.kDisplacement);
            encoderAvg.setPIDSourceType(PIDSourceType.kDisplacement);
            // Set up rotation PID controller
            rotController = new PIDController(Calibration.DRIVE_ROTATE_PID_P,
                    Calibration.DRIVE_ROTATE_PID_I,
                    Calibration.DRIVE_ROTATE_PID_D,
                    encoderDiff,
                    rotateBot,
                    Calibration.DRIVE_PID_SAMPLE_RATE);
            rotController.setSetpoint(rotSetpoint);
            rotController.setOutputRange(-Calibration.DRIVE_ROTATE_PID_MAX,
                    Calibration.DRIVE_ROTATE_PID_MAX);
            rotController.setAbsoluteTolerance(Calibration.DRIVE_ROTATE_TOLERANCE);
            // Set up move PID controller
            moveController = new PIDController(Calibration.DRIVE_MOVE_PID_P,
                    Calibration.DRIVE_MOVE_PID_I,
                    Calibration.DRIVE_MOVE_PID_D,
                    encoderAvg,
                    moveBot,
                    Calibration.DRIVE_PID_SAMPLE_RATE);
            moveController.setSetpoint(moveSetpoint);
            moveController.setOutputRange(-Calibration.DRIVE_MOVE_PID_MAX,
                    Calibration.DRIVE_MOVE_PID_MAX);
            moveController.setAbsoluteTolerance(Calibration.DRIVE_MOVE_TOLERANCE);
            arcadePIDLog.log("INFO", "Enabling rotation controller");
            rotController.enable();
            // Stagger the timings of the PIDs slightly
            try {
                // 500 = 1000 / 2
                // Set up PIDs to output in even staggering
                Thread.sleep((long) (Calibration.DRIVE_PID_SAMPLE_RATE*500));
            } catch (InterruptedException e) {
                // Do nothing
            }
            arcadePIDLog.log("INFO", "Enabling move controller");
            moveController.enable();
            // Instead of using complex logic to start timer,
            // Start timer here and constantly reset until setpoint is reached
            timeElapsed.start();
            PIDTargetPulse.update(true);
        }

        @Override
        protected synchronized boolean run() {
            arcadeDrive.activate();
            // System.out.println("Move error is " + getMoveError() + ", Rot error is " + getRotError());
            return timeElapsed.runUntil(Calibration.DRIVE_PID_AFTER_TIMING, new Runnable() {
                @Override
                public void run() {
                    boolean targetReached = rotController.onTarget() && moveController.onTarget();
                    if (!targetReached) {
                        timeElapsed.reset();
                        PIDTargetPulse.update(true);
                    } else {
                        PIDTargetPulse.update(false);
                    }
                    if (PIDTargetPulse.isFallingEdge()) {
                        arcadePIDLog.log("INFO", "Target reached, enabling timer");
                    } else if (PIDTargetPulse.isRisingEdge()) {
                        arcadePIDLog.log("INFO", "Target left, disabling timer");
                    }
                }
            });
        }

        @Override
        protected void end() {
            arcadePIDLog.log("INFO","Final Rotate error is "+rotController.getError());
            arcadePIDLog.log("INFO","Final Move error is "+moveController.getError());
            rotController.disable();
            moveController.disable();
            timeElapsed.stopAndReset();
        }
    }
    
    /* Modular Modes */
    private class CenterSwitchMacro extends SwitchCoordinator {
    	public CenterSwitchMacro() {
    		super(CenterSwitchMacro.class);
    		addCase(new String[]{"LLL", "LLR", "LRL", "LRR"}, new CenterMacroLeft());
    		addCase(new String[]{"RLL", "RLR", "RRL", "RRR"}, new CenterMacroRight());
    	}
    }
    
    private class LeftScaleMacro extends SwitchCoordinator {
    	public LeftScaleMacro() {
    		super(LeftScaleMacro.class);
    		addCase(new String[]{"LLL", "LLR", "RLL", "RLR"}, new NewScaleBackwardMacroLeft());
    		addCase(new String[]{"LRL", "LRR", "RRL", "RRR"}, new SwerveScaleOppositeMacroLeft());
    	}
    }
    
    private class RightScaleMacro extends SwitchCoordinator {
    	public RightScaleMacro() {
    		super(RightScaleMacro.class);
    		addCase(new String[]{"LLL", "LLR", "RLL", "RLR"}, new SwerveScaleOppositeMacroRight());
    		addCase(new String[]{"LRL", "LRR", "RRL", "RRR"}, new NewScaleBackwardMacroRight());
    	}
    }
    
    /* Auton Modes */
    private class SwitchForwardMacro extends StatefulCoordinator {
        public SwitchForwardMacro() {
            super(SwitchForwardMacro.class);
            addStates(new IntakeMacro());
            addState("Forward 80 in", new ArcadePIDCoordinator(AutonMovement.inchesToClicks(Calibration.DRIVE_PROPERTIES, 80+1), 0));
            addState("Sleep 0.5 seconds", new SleepCoordinator(0.5));
            addStates(new SwitchEjectMacro());
            //addState("Rotate 180 left", new ArcadePIDCoordinator(0,AutonMovement.degreesToClicks(Calibration.DRIVE_PROPERTIES, -180)));
            //addState("Sleep 0.5 seconds", new SleepCoordinator(0.5));
            //addState("Forward 12 feet", new ArcadePIDCoordinator(AutonMovement.inchesToClicks(Calibration.DRIVE_PROPERTIES, 12*12+1), 0));
        }
    }
    
    private class CenterMacroLeft extends StatefulCoordinator {
        public CenterMacroLeft() {
            super(CenterMacroLeft.class);
            addState("Running left", new SleepCoordinator(0.1));
            addStates(new IntakeMacro());
            addState("Forward 10 inches", new ArcadePIDCoordinator(AutonMovement.inchesToClicks(Calibration.DRIVE_PROPERTIES, 10+1),0));
            addState("Rotate 45 left", new ArcadePIDCoordinator(0, AutonMovement.degreesToClicks(Calibration.DRIVE_PROPERTIES, -45)));
            addState("Forward 69*sqrt(2) inches", new ArcadePIDCoordinator(AutonMovement.inchesToClicks(Calibration.DRIVE_PROPERTIES, 69*Math.sqrt(2)+1),0));
            addState("Rotate 45 right", new ArcadePIDCoordinator(0, AutonMovement.degreesToClicks(Calibration.DRIVE_PROPERTIES, 45)));
            //addState("Forward 19 inches", new ArcadePIDCoordinator(AutonMovement.inchesToClicks(Calibration.DRIVE_PROPERTIES, 19+1),0));
            addStates(new SwitchEjectMacro());
        }
    }
    
    private class CenterMacroRight extends StatefulCoordinator {
        public CenterMacroRight() {
            super(CenterMacroRight.class);
            addState("Running right", new SleepCoordinator(0.1));
            addStates(new IntakeMacro());
            addState("Forward 10 inches", new ArcadePIDCoordinator(AutonMovement.inchesToClicks(Calibration.DRIVE_PROPERTIES, 10+1),0));
            addState("Rotate 45 right", new ArcadePIDCoordinator(0, AutonMovement.degreesToClicks(Calibration.DRIVE_PROPERTIES, 45)));
            addState("Forward 69*sqrt(2) inches", new ArcadePIDCoordinator(AutonMovement.inchesToClicks(Calibration.DRIVE_PROPERTIES, 69*Math.sqrt(2)+1),0));
            addState("Rotate 45 left", new ArcadePIDCoordinator(0, AutonMovement.degreesToClicks(Calibration.DRIVE_PROPERTIES, -45)));
            //addState("Forward 19 inches", new ArcadePIDCoordinator(AutonMovement.inchesToClicks(Calibration.DRIVE_PROPERTIES, 19+1),0));
            addStates(new SwitchEjectMacro());
        }
    }
    
    @Unreal("Old legacy testing of previous distances")
    private class ScaleBackwardMacro extends StatefulCoordinator {
        public ScaleBackwardMacro() {
            super(ScaleBackwardMacro.class);
            addStates(new IntakeMacro());
            // backward 310
            addState("Backward 307 inches", new ArcadePIDCoordinator(AutonMovement.inchesToClicks(Calibration.DRIVE_PROPERTIES, -(307+1)), 0));
            // rotate 90 right
            addState("Rotate 90 right", new ArcadePIDCoordinator(0, AutonMovement.degreesToClicks(Calibration.DRIVE_PROPERTIES, 90)));
            // Eject
            addStates(new ScaleEjectMacro());
        }
    }
    
    @Unreal("Old legacy testing of previous distances")
    private class ScaleBackwardMacro2 extends StatefulCoordinator {
        public ScaleBackwardMacro2() {
            super(ScaleBackwardMacro2.class);
            addStates(new IntakeMacro());
            addState("Backward 262 inches", new ArcadePIDCoordinator(AutonMovement.inchesToClicks(Calibration.DRIVE_PROPERTIES, -(262+1)), 0));
            addState("Rotate 35 right", new ArcadePIDCoordinator(0, AutonMovement.degreesToClicks(Calibration.DRIVE_PROPERTIES, 35)));
            // Eject
            addStates(new ScaleEjectMacro());
        }
    }
    
    private class NewScaleBackwardMacroLeft extends StatefulCoordinator {
    	public NewScaleBackwardMacroLeft() {
    		super(NewScaleBackwardMacroLeft.class);
    		addStates(new IntakeMacro());
    		addState("Set Elevator Persistent", new ElevatorSetPersistent(Calibration.ELEVATOR_MID_TARGET));
            addState("Backward 262 inches", new ArcadePIDCoordinator(AutonMovement.inchesToClicks(Calibration.DRIVE_PROPERTIES, -(262+1)), 0));
            addState("Set Arm Persistent", new ArmSetPersistent(Calibration.ARM_BALANCE_TARGET));
            addState("Sleep 0.5 seconds", new SleepCoordinator(0.5));
            addState("Rotate 35 right", new ArcadePIDCoordinator(0, AutonMovement.degreesToClicks(Calibration.DRIVE_PROPERTIES, 35)));
            addState("Set Arm High Persistent", new ArmSetPersistent(Calibration.ARM_HIGH_TARGET));
            addState("Sleep 1 second", new SleepCoordinator(1.5));
            addState("Eject cube", new IntakeMove(-1,0.5));
            addState("Retract arm", new ArmSetPersistent(Calibration.ARM_LOW_TARGET));
            addState("Retract elevator", new ElevatorSetPersistent(Calibration.ELEVATOR_LOW_TARGET));
            addState("Wait for arm", new SleepCoordinator(0.7));
            addState("Unclamp", new ClampExtend());
    	}
    }
    
    private class NewScaleBackwardMacroRight extends StatefulCoordinator {
    	public NewScaleBackwardMacroRight() {
    		super(NewScaleBackwardMacroRight.class);
    		addStates(new IntakeMacro());
    		addState("Set Elevator Persistent", new ElevatorSetPersistent(Calibration.ELEVATOR_MID_TARGET));
            addState("Backward 262 inches", new ArcadePIDCoordinator(AutonMovement.inchesToClicks(Calibration.DRIVE_PROPERTIES, -(262+1)), 0));
            addState("Set Arm Persistent", new ArmSetPersistent(Calibration.ARM_BALANCE_TARGET));
            addState("Sleep 0.5 seconds", new SleepCoordinator(0.5));
            addState("Rotate 35 left", new ArcadePIDCoordinator(0, AutonMovement.degreesToClicks(Calibration.DRIVE_PROPERTIES, -35)));
            addState("Set Arm High Persistent", new ArmSetPersistent(Calibration.ARM_HIGH_TARGET));
            addState("Sleep 1 second", new SleepCoordinator(1.5));
            addState("Eject cube", new IntakeMove(-1,0.5));
            addState("Retract arm", new ArmSetPersistent(Calibration.ARM_LOW_TARGET));
            addState("Retract elevator", new ElevatorSetPersistent(Calibration.ELEVATOR_LOW_TARGET));
            addState("Wait for arm", new SleepCoordinator(0.7));
            addState("Unclamp", new ClampExtend());
    	}
    }
    
    
    @Unreal("Old legacy code without swerve")
    private class ScaleOppositeMacroLeft extends StatefulCoordinator {
        public ScaleOppositeMacroLeft() {
            super(ScaleOppositeMacroLeft.class);
            addStates(new IntakeMacro());
            addState("Set Elevator Persistent", new ElevatorSetPersistent(Calibration.ELEVATOR_MID_TARGET));
            addState("Backward 227 inches", new ArcadePIDCoordinator(AutonMovement.inchesToClicks(Calibration.DRIVE_PROPERTIES, -(227+1)), 0));
            addState("Set Arm Persistent", new ArmSetPersistent(Calibration.ARM_BALANCE_TARGET));
            addState("Sleep 0.5 seconds", new SleepCoordinator(0.5));
            addState("Rotate 90 left", new ArcadePIDCoordinator(0, AutonMovement.degreesToClicks(Calibration.DRIVE_PROPERTIES, -100)));
            addState("Forward 176 inches", new ArcadePIDCoordinator(AutonMovement.inchesToClicks(Calibration.DRIVE_PROPERTIES, (176+1)), 0));
            addState("Rotate 90 right", new ArcadePIDCoordinator(0, AutonMovement.degreesToClicks(Calibration.DRIVE_PROPERTIES, 90)));
            addState("Set Arm High Persistent", new ArmSetPersistent(Calibration.ARM_HIGH_TARGET));
            addState("Backward 27 inches", new ArcadePIDCoordinator(AutonMovement.inchesToClicks(Calibration.DRIVE_PROPERTIES, -(27+1)), 0));
            addState("Eject cube", new IntakeMove(-1,1));
            addState("Retract arm", new ArmSetPersistent(Calibration.ARM_LOW_TARGET));
            addState("Retract elevator", new ElevatorSetPersistent(Calibration.ELEVATOR_LOW_TARGET));
            addState("Wait for reset", new SleepCoordinator(0.7));
            addState("Unclamp", new ClampExtend());
        }
    }
    
    private class SwerveScaleOppositeMacroLeft extends StatefulCoordinator {
        public SwerveScaleOppositeMacroLeft() {
            super(SwerveScaleOppositeMacroLeft.class);
            addStates(new IntakeMacro());
            addState("Set Elevator Persistent", new ElevatorSetPersistent(Calibration.ELEVATOR_MID_TARGET));
            addState("Backward 149 inches", new ArcadePIDCoordinator(AutonMovement.inchesToClicks(Calibration.DRIVE_PROPERTIES, -(149+1)), 0));
            addState("Set Arm Persistent", new ArmSetPersistent(Calibration.ARM_BALANCE_TARGET));
            //addState("Sleep 0.5 seconds", new SleepCoordinator(0.5));
            addState("Sweep 6 feet backwards, 90 left", new ArcadePIDCoordinator(AutonMovement.inchesToClicks(Calibration.DRIVE_PROPERTIES, -72), AutonMovement.degreesToClicks(Calibration.DRIVE_PROPERTIES, 90)));
            //addState("Rotate 90 left", new ArcadePIDCoordinator(0, AutonMovement.degreesToClicks(Calibration.DRIVE_PROPERTIES, -100)));
            addState("Backward 130 inches", new ArcadePIDCoordinator(AutonMovement.inchesToClicks(Calibration.DRIVE_PROPERTIES, -(130+1)), 0));
            addState("Rotate 90 left", new ArcadePIDCoordinator(0, AutonMovement.degreesToClicks(Calibration.DRIVE_PROPERTIES, -90)));
            addState("Set Arm High Persistent", new ArmSetPersistent(Calibration.ARM_HIGH_TARGET));
            addState("Backward 27 inches", new ArcadePIDCoordinator(AutonMovement.inchesToClicks(Calibration.DRIVE_PROPERTIES, -(27+1)), 0));
            addState("Eject cube", new IntakeMove(-1,0.25));
            addState("Retract arm", new ArmSetPersistent(Calibration.ARM_LOW_TARGET));
            addState("Retract elevator", new ElevatorSetPersistent(Calibration.ELEVATOR_LOW_TARGET));
            addState("Unclamp", new ClampExtend());
            addState("Wait for reset", new SleepCoordinator(0.7));
        }
    }
    
    private class SwerveScaleOppositeMacroRight extends StatefulCoordinator {
        public SwerveScaleOppositeMacroRight() {
            super(SwerveScaleOppositeMacroRight.class);
            addStates(new IntakeMacro());
            addState("Set Elevator Persistent", new ElevatorSetPersistent(Calibration.ELEVATOR_MID_TARGET));
            addState("Backward 149 inches", new ArcadePIDCoordinator(AutonMovement.inchesToClicks(Calibration.DRIVE_PROPERTIES, -(149+1)), 0));
            addState("Set Arm Persistent", new ArmSetPersistent(Calibration.ARM_BALANCE_TARGET));
            //addState("Sleep 0.5 seconds", new SleepCoordinator(0.5));
            addState("Sweep 6 feet backwards, 90 right", new ArcadePIDCoordinator(AutonMovement.inchesToClicks(Calibration.DRIVE_PROPERTIES, 72), AutonMovement.degreesToClicks(Calibration.DRIVE_PROPERTIES, 90)));
            //addState("Rotate 90 left", new ArcadePIDCoordinator(0, AutonMovement.degreesToClicks(Calibration.DRIVE_PROPERTIES, -100)));
            addState("Backward 130 inches", new ArcadePIDCoordinator(AutonMovement.inchesToClicks(Calibration.DRIVE_PROPERTIES, -(130+1)), 0));
            addState("Rotate 90 right", new ArcadePIDCoordinator(0, AutonMovement.degreesToClicks(Calibration.DRIVE_PROPERTIES, 90)));
            addState("Set Arm High Persistent", new ArmSetPersistent(Calibration.ARM_HIGH_TARGET));
            addState("Backward 27 inches", new ArcadePIDCoordinator(AutonMovement.inchesToClicks(Calibration.DRIVE_PROPERTIES, -(27+1)), 0));
            addState("Eject cube", new IntakeMove(-1,0.25));
            addState("Retract arm", new ArmSetPersistent(Calibration.ARM_LOW_TARGET));
            addState("Retract elevator", new ElevatorSetPersistent(Calibration.ELEVATOR_LOW_TARGET));
            addState("Unclamp", new ClampExtend());
            addState("Wait for reset", new SleepCoordinator(0.7));
        }
    }
    
    @Unreal("Old legacy testing")
    private class SideLeftMacro extends StatefulCoordinator {
        public SideLeftMacro() {
            super(SideLeftMacro.class);
            addState("Backward 210 inches", new ArcadePIDCoordinator(AutonMovement.inchesToClicks(Calibration.DRIVE_PROPERTIES, -210), 0));
            //addState("Sleep 0.5 seconds", new SleepCoordinator(0.5));
            addState("Rotate 90 right", new ArcadePIDCoordinator(0,AutonMovement.degreesToClicks(Calibration.DRIVE_PROPERTIES, 90)));
            //addState("Sleep 0.5 seconds", new SleepCoordinator(0.5));
            addState("Backward 69 inches", new ArcadePIDCoordinator(AutonMovement.inchesToClicks(Calibration.DRIVE_PROPERTIES, -69), 0));
            addState("Rotate 90 left", new ArcadePIDCoordinator(0,AutonMovement.degreesToClicks(Calibration.DRIVE_PROPERTIES, -90)));
            addState("Forward 14 inches", new ArcadePIDCoordinator(AutonMovement.inchesToClicks(Calibration.DRIVE_PROPERTIES, 14), 0));
        }
    }
    
    /* Debug */
    @Unreal("Old legacy testing kept for debugging purposes")
    private class SimultaneousMacro extends StatefulCoordinator {
    	public SimultaneousMacro() {
    		super(SimultaneousMacro.class);
    		addState("Intake cube", new IntakeMacro());
    		addState("Set Elevator Persistent", new ElevatorSetPersistent(Calibration.ELEVATOR_HIGH_TARGET));
    		addState("Set Arm Persistent", new ArmSetPersistent(Calibration.ARM_HIGH_TARGET));
    		addState("Sleep 3 seconds", new SleepCoordinator(3));
    		addState("Eject cube", new IntakeMove(-0.5, 1));
    	}
    }
    
    @Unreal("Old legacy testing kept for debugging purposes")
    private class BalancedLeftTurnMacro extends StatefulCoordinator {
    	public BalancedLeftTurnMacro() {
    		super(BalancedLeftTurnMacro.class);
    		addState("Set Arm Persistent", new ArmSetPersistent(Calibration.ARM_BALANCE_TARGET));
            addState("Sleep 0.5 seconds", new SleepCoordinator(0.5));
    		addState("Rotate 90 left", new ArcadePIDCoordinator(0, AutonMovement.degreesToClicks(Calibration.DRIVE_PROPERTIES, -90)));
    		addState("Retract arm", new ArmSetPersistent(Calibration.ARM_LOW_TARGET));
    	}
    }
    
    @Unreal("Old legacy testing")
    private class SweptLeftTurnMacro extends StatefulCoordinator {
    	public SweptLeftTurnMacro() {
    		super(SweptLeftTurnMacro.class);
    		addState("Sweep 3 feet forward, 90 left", new ArcadePIDCoordinator(AutonMovement.inchesToClicks(Calibration.DRIVE_PROPERTIES, 72), AutonMovement.degreesToClicks(Calibration.DRIVE_PROPERTIES, -90)));
    	}
    }
    
    @Unreal("Old legacy testing")
    private class BalancedSweptLeftTurnMacro extends StatefulCoordinator {
    	public BalancedSweptLeftTurnMacro() {
    		super(BalancedSweptLeftTurnMacro.class);
    		addState("Set Arm Persistent", new ArmSetPersistent(Calibration.ARM_BALANCE_TARGET));
            addState("Sleep 0.5 seconds", new SleepCoordinator(0.5));
    		addState("Sweep 6 feet forward, 90 left", new ArcadePIDCoordinator(AutonMovement.inchesToClicks(Calibration.DRIVE_PROPERTIES, 72), AutonMovement.degreesToClicks(Calibration.DRIVE_PROPERTIES, -90)));
            addState("Retract arm", new ArmSetPersistent(Calibration.ARM_LOW_TARGET));
    	}
    }
    
    @Unreal("Old legacy testing")
    private class BalancedRightTurnMacro extends StatefulCoordinator {
    	public BalancedRightTurnMacro() {
    		super(BalancedRightTurnMacro.class);
    		addState("Set Arm Persistent", new ArmSetPersistent(Calibration.ARM_BALANCE_TARGET));
            addState("Sleep 0.5 seconds", new SleepCoordinator(0.5));
    		addState("Rotate 90 Right", new ArcadePIDCoordinator(0, AutonMovement.degreesToClicks(Calibration.DRIVE_PROPERTIES, 90)));
            addState("Retract arm", new ArmSetPersistent(Calibration.ARM_LOW_TARGET));

    	}
    }
    
    @Unreal("Old legacy testing")
    private class SweptRightTurnMacro extends StatefulCoordinator {
    	public SweptRightTurnMacro() {
    		super(SweptRightTurnMacro.class);
    		addState("Sweep 6 feet forward, 90 Right", new ArcadePIDCoordinator(AutonMovement.inchesToClicks(Calibration.DRIVE_PROPERTIES, 72), AutonMovement.degreesToClicks(Calibration.DRIVE_PROPERTIES, 90)));
    	}
    }
    @Unreal("Old legacy testing")
    private class BalancedSweptRightTurnMacro extends StatefulCoordinator {
    	public BalancedSweptRightTurnMacro() {
    		super(BalancedSweptRightTurnMacro.class);
    		addState("Set Arm Persistent", new ArmSetPersistent(Calibration.ARM_BALANCE_TARGET));
            addState("Sleep 0.5 seconds", new SleepCoordinator(0.5));
    		addState("Sweep 6 feet forward, 90 Right", new ArcadePIDCoordinator(AutonMovement.inchesToClicks(Calibration.DRIVE_PROPERTIES, 72), AutonMovement.degreesToClicks(Calibration.DRIVE_PROPERTIES, 100)));
    		addState("Retract arm", new ArmSetPersistent(Calibration.ARM_LOW_TARGET));
    	}
    }
    
    private class DemoStateMacro extends StatefulCoordinator {
        public DemoStateMacro() {
            super(DemoStateMacro.class);
            addStates(new IntakeMacro());
            addState("Forward and Elevator", new SimultaneousCoordinator(
                    new ArcadePIDCoordinator(AutonMovement.inchesToClicks(Calibration.DRIVE_PROPERTIES, 140+1), 0),
                    new ElevatorMove(Calibration.ELEVATOR_MID_TARGET,7)
                    ));
            addState("Eject cube", new IntakeMove(-0.5,1));
            //addState("Move arm", new ArmMove(Calibration.ARM_LOW_TARGET, 0.5));
            addState("Unclamp", new ClampExtend());
            //addState("Sleep 0.5 seconds", new SleepCoordinator(0.5));
            //addStates(new SwitchEjectMacro());
            //addState("Rotate 180 left", new ArcadePIDCoordinator(0,AutonMovement.degreesToClicks(Calibration.DRIVE_PROPERTIES, -180)));
            //addState("Sleep 0.5 seconds", new SleepCoordinator(0.5));
            //addState("Forward 12 feet", new ArcadePIDCoordinator(AutonMovement.inchesToClicks(Calibration.DRIVE_PROPERTIES, 12*12+1), 0));
        }
    }
    
    /* Utilities */   
    private class IntakeMacro extends StatefulCoordinator {
        public IntakeMacro() {
            super(IntakeMacro.class);
            addState("Intake cube", new IntakeMove(0.5,0.2));
            //addState("Sleep 0.25 seconds", new SleepCoordinator(0.25));
            addState("Clamp cube", new ClampRetract());
        }
    }
    
    private class SwitchEjectMacro extends StatefulCoordinator {
        public SwitchEjectMacro() {
            super(SwitchEjectMacro.class);
            addState("Move elevator", new ElevatorMove(Calibration.ELEVATOR_SWITCH_CLEAR, 1));
            addState("Move arm", new ArmSetPersistent(Calibration.ARM_MID_TARGET));
            addState("Wait", new SleepCoordinator(0.8));
            addState("Eject cube", new IntakeMove(-0.5,0.5));
            // addState("Move elevator", new ElevatorMove(Calibration.ELEVATOR_BUMPER_CLEAR, 1));
            // Arm falls down
            addState("Move arm", new ArmSetPersistent(Calibration.ARM_LOW_TARGET));
            addState("Wait", new SleepCoordinator(0.5));
            addState("Unclamp", new ClampExtend());
        }
    }
    
    @Unreal("Legacy")
    private class ScaleEjectMacro extends StatefulCoordinator {
        public ScaleEjectMacro() {
            super(ScaleEjectMacro.class);
            addState("Move elevator", new ElevatorMove(Calibration.ELEVATOR_BUMPER_CLEAR, 0.5));
            addState("Move arm", new ArmMove(Calibration.ARM_HIGH_TARGET, 2));
            addState("Eject cube", new IntakeMove(-1,1));
            addState("Move arm", new ArmMove(Calibration.ARM_LOW_TARGET, 0.7));
            addState("Unclamp", new ClampExtend());
        }
    }
    
    private class ArcadePIDStateMacro extends StatefulCoordinator {
        private Logger arcadePIDLog=new Logger("ArcadePIDStateMacro");
        private ArcadePIDCoordinator controller;
        public ArcadePIDStateMacro (double moveSetpoint, double rotSetpoint) {
            super(ArcadePIDStateMacro.class);
            arcadePIDLog.log("INFO", "Move Setpoint is "+moveSetpoint);
            arcadePIDLog.log("INFO", "Rotate setpoint is "+rotSetpoint);
            // Set up a rotate class with both Move and Rotate PIDs
            // Instead of just setting a difference, actively move forwards/backwards to compensate for REAL life
            // This avoids COMPLEX imperfections and reduces many issues to be purely IMAGINARY
            controller = new ArcadePIDCoordinator(moveSetpoint, rotSetpoint);
            addState("ArcadePID",controller);
        }
    }

}