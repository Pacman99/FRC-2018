package com._604robotics.robot2018.modules;

import com._604robotics.robot2018.constants.Calibration;
import com._604robotics.robot2018.constants.Ports;
import com._604robotics.robotnik.Action;
import com._604robotics.robotnik.Input;
import com._604robotics.robotnik.Module;
import com._604robotics.robotnik.Output;
import com._604robotics.robotnik.prefabs.controller.ClampedIntegralPIDController;
import com._604robotics.robotnik.prefabs.devices.TalonPWMEncoder;
import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.can.WPI_TalonSRX;

public class Elevator extends Module {

    public WPI_TalonSRX motorA = new WPI_TalonSRX(Ports.ELEVATOR_MOTOR_A);
    public WPI_TalonSRX motorB = new WPI_TalonSRX(Ports.ELEVATOR_MOTOR_B);
    public TalonPWMEncoder encoder = new TalonPWMEncoder(motorA);

    public final Setpoint setpoint = new Setpoint();

    public final Output<Double> encoderRate = addOutput("Elevator Rate", encoder::getVelocity);
    public final Output<Double> encoderClicks = addOutput("Elevator Clicks", encoder::getPosition);

    public boolean holding = true;
    public double power = 0;

    public final Output<Boolean> getHolding = addOutput("Holding", this::getHolding);
    public final Output<Double> getPower = addOutput("Power", this::getPower);

    private final ClampedIntegralPIDController pid;
    
    public final Output<Double> pidError;

    public boolean getHolding() {
        return holding;
    }

    public double getPower() {
        return power;
    }
    
    public void resetIntegral(double sum) {
        pid.setErrorSum(sum);
    }

    public class Move extends Action {
        public final Input<Double> liftPower;

        public Move() {
            this(0);
        }

        public Move (double power) {
            super(Elevator.this, Move.class);
            liftPower = addInput("Lift Power", power, true);
        }

        @Override
        public void run () {
            holding = false;
            power = liftPower.get();
            motorA.set(liftPower.get());
        }
    }

    public class Setpoint extends Action {
        public final Input<Double> target_clicks;

        public Setpoint() {
            this(0);
        }

        public Setpoint(double clicks) {
            super(Elevator.this, Setpoint.class);
            target_clicks = addInput("Target Clicks", clicks, true);
        }

        @Override
        public void begin() {
            pid.enable();
            holding = false;
        }
        @Override
        public void run () {
            pid.setSetpoint(target_clicks.get());
        }
        @Override
        public void end () {
            pid.disable();
        }
    }

    public Elevator() {
        super(Elevator.class);
        encoder.setInverted(false);
        motorA.setInverted(true);
        motorB.setInverted(true);
        motorB.set(ControlMode.Follower,Ports.ELEVATOR_MOTOR_A);
        pid = new ClampedIntegralPIDController(Calibration.ELEVATOR_P,
                Calibration.ELEVATOR_I,
                Calibration.ELEVATOR_D,
                encoder,
                motorA,
                Calibration.ELEVATOR_PID_PERIOD);
        pidError = addOutput("Elevator PID Error", pid::getError);
        pid.setIntegralLimits(Calibration.ELEVATOR_MIN_SUM, Calibration.ELEVATOR_MAX_SUM);
        pid.setOutputRange(Calibration.ELEVATOR_MIN_SPEED, Calibration.ELEVATOR_MAX_SPEED);
        setpoint.target_clicks.set(encoder.getPosition());
        setDefaultAction(setpoint);
    }
}
