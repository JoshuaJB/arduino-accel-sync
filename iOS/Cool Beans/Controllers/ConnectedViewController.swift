//
//  ConnectedViewController.swift
//  Cool Beans
//
//  Created by Kyle on 11/14/14.
//  Copyright (c) 2014 Kyle Weiner. All rights reserved.
//

import UIKit
import Foundation

var someBytes = NSMutableData.init(capacity: 4);
var byteCounter = 0;

struct Temperature {
    enum State {
        case Unknown
        case Cold
        case Cool
        case Warm
        case Hot
    }

    var degreesCelcius: Float
    var degressFahrenheit: Float {
        return (degreesCelcius * 1.8) + 32.0
    }
    

    func state() -> State {
        let fahrenheit = degressFahrenheit
        switch Int(fahrenheit) {
            case let x where fahrenheit <= 39:
                return .Cold
            case 40...65:
                return .Cool
            case 66...80:
                return .Warm
            case let x where fahrenheit >= 81:
                return .Hot
            default:
                return .Unknown
        }
    }
}

class ConnectedViewController: UIViewController, PTDBeanDelegate {
    
    

    let refreshControl = UIRefreshControl()

    var connectedBean: PTDBean?
    var currentTemperature: Temperature = Temperature(degreesCelcius: 0.0) {
        didSet {
            updateTemperatureView()
            updateBean()
        }
    }

    @IBOutlet weak var scrollView: UIScrollView!
    @IBOutlet weak var temperatureView: TemperatureView!

    // MARK: Lifecycle

    override func viewDidLoad() {
        super.viewDidLoad()

        // Update the name label.
        temperatureView.nameLabel.text = connectedBean?.name

        // Add pull-to-refresh control.
        refreshControl.addTarget(self, action: "didPullToRefresh:", forControlEvents: .ValueChanged)
        refreshControl.tintColor = .whiteColor()
        scrollView.addSubview(refreshControl)
    }

    override func viewWillAppear(animated: Bool) {
        super.viewWillAppear(animated)
        connectedBean?.readAccelerationAxes()
    }

    // MARK: Actions

    func didPullToRefresh(sender: AnyObject) {
        refreshControl.endRefreshing()
        connectedBean?.readAccelerationAxes()
    }

    // MARK: PTDBeanDelegate

    func bean(bean: PTDBean!, didUpdateTemperature degrees_celsius: NSNumber!) {
    }

    func bean(bean: PTDBean!, didUpdateAccelerationAxes acceleration: PTDAcceleration){
        //print("x:%d y:%d z: %d", acceleration.x, acceleration.y, acceleration.z)
        temperatureView.temperatureLabel.text = String(format: "x:%d y:%d z: %d", acceleration.x, acceleration.y, acceleration.z)
    }
    
   
    /**************************************************************************************/
    func bean(bean: PTDBean!, serialDataReceived data: NSData!){
        var g = 0;
        /**
        * All accelerometer data/timesync messages are 32 / 8 or 4 bytes
        * long.
        * The following code collects individual bytes and waits for chunks of
        * four to be ready for interpretation.
        */
        if (data.length == 1) {
            
            NSLog("Unhandled serial message: " + String(data))
            var hexString = String(data);
         
                var firstChar =  hexString.removeAtIndex(hexString.startIndex.advancedBy(1))
                var secondChar = hexString.removeAtIndex(hexString.startIndex.advancedBy(2))
            
            
            
            if (secondChar == "1"){
                g += 1*1
            }
            if (secondChar == "2"){
                g += 2*1
            }
            if (secondChar == "3"){
                g += 3*1
            }
            if (secondChar == "4"){
                g += 4*1
            }
            if (secondChar == "5"){
                g += 5*1
            }
            if (secondChar == "6"){
                g += 6*1
            }
            if (secondChar == "7"){
                g += 7*1
            }
            if (secondChar == "8"){
                g += 8*1
            }
            if (secondChar == "9"){
                g += 9*1
            }
            if (secondChar == "a"){
                g += 10*1
            }
            if (secondChar == "b"){
                g += 11*1
            }
            if (secondChar == "c"){
                g += 12*1
            }
            if (secondChar == "d"){
                g += 13*1
            }
            if (secondChar == "e"){
                g += 14*1
            }
            if (secondChar == "f"){
                g += 15*1
            }
            
                if (firstChar == "1"){
                g += 1*16
                }
                if (firstChar == "2"){
                g += 2*16
                }
                if (firstChar == "3"){
                g += 3*16
                }
                if (firstChar == "4"){
                g += 4*16
                }
                if (firstChar == "5"){
                g += 5*16
                }
                if (firstChar == "6"){
                g += 6*16
                }
                if (firstChar == "7"){
                g += 7*16
                }
                if (firstChar == "8"){
                g *= -1
                }
                if (firstChar == "9"){
                    g += 16; g *= -1;
                }
                if (firstChar == "a"){
                    g += 16*2; g *= -1;
                }
                if (firstChar == "b"){
                    g += 16*3; g *= -1;
                }
                if (firstChar == "c"){
                    g += 16*4; g *= -1;
                }
                if (firstChar == "d"){
                    g += 16*5; g *= -1;
                }
                if (firstChar == "e"){
                    g += 16*6; g *= -1;
                }
                if (firstChar == "f"){
                    g += 16*7; g *= -1;
                }
            
            
           
            
            
                //if the char is numeric, leave it, else turn it into the right integer and multiply
            
            
            temperatureView.temperatureLabel.text = String(format: "%i", g)
            NSLog("Message Length: %i" ,data.length)
            
            return;
        }
      
            someBytes?.appendBytes(data.bytes, length: 1)
            byteCounter++;
            if (byteCounter == 4){
                byteCounter = 0;
                //now use Joshua's code on the array 'someBytes'
              //  if ((someBytes. & 0x000000FF) >= 0x000000C0){
                    /**
                    * The following comments use left
                    * to right bit numbering starting at 0. (bits 0-1 are signal)
                    */
                    // The X reading is bits 2-11. The 6 rightmost bits from byte
                    // 0 and the 4 leftmost bits from byte 1.
                    
               //     var xbytes = NSData(bytes: [UInt8(someBytes[0]) & 0x3F >> 4,
                        //UInt8(someBytes[1]) & 0xF0 >> 4] as [UInt8], length: 2)
                    // The Y reading is bits 12-21. The 4 rightmost bits from byte
                    // 1 and the 6 leftmost bits from byte 2.
               //    var ybytes = NSData(bytes: [UInt8(someBytes[1]) & 0x0F >> 2,
                 //       UInt8(someBytes[2]) & 0xFC >> 2] as [UInt8], length: 2)
                    // The Z reading is bits 22-31. The 2 rightmost bits from byte
                    // 2 and all the bits from from byte 3.
                   // var zbytes = NSData(bytes: [UInt8(someBytes[2]) & 0x03,
                     //   UInt8(someBytes[3])] as [UInt8], length: 2)
                    
                   // NSLog("%i, %i, %i", xbytes,ybytes,zbytes)
                   // temperatureView.temperatureLabel.text = String(format: "x:%s y:%s z: %s", //String(xbytes),String(ybytes),String(zbytes))
                    return;
              //  }
                // It's a timesync message
                //TODO
                return;
            }
        
    }
    
    

    // MARK: Helper

    func updateTemperatureView() {
        // Update the temperature label.
        //temperatureView.temperatureLabel.text = String(format: "%.f℉", currentTemperature.degressFahrenheit)

        // Update the background color.
        var backgroundColor: UIColor

        switch currentTemperature.state() {
            case .Unknown:
                backgroundColor = .blackColor()
            case .Cold:
                backgroundColor = .CBColdColor()
            case .Cool:
                backgroundColor = .CBCoolColor()
            case .Warm:
                backgroundColor = .CBWarmColor()
            case .Hot:
                backgroundColor = .CBHotColor()
        }

        UIView.animateWithDuration(0.4, animations: {
            self.scrollView.backgroundColor = backgroundColor
            self.temperatureView.containerView.backgroundColor = backgroundColor
        })
    }
    
  /*  func decodeBytes (bytes: NSData!) -> UInt8  {
    /* Note on Java and Bitwise Operators
    *  Java bitwise operators only work on ints and longs,
    *  Bytes will undergo promotion with sign extension first.
    *  So, we have to undo the sign extension on the lower order
    *  bits here.
    */
    var ans = UInt8(someBytes[0]);
    for var i = 1; i < someBytes.count; i++ {
    ans <<= 8;
    ans |= UInt8(someBytes[i]) & 0xFF;
    }
    return ans;
    }
*/
    func updateBean() {
       // connectedBean?.setLedColor(temperatureView.containerView.backgroundColor)
    }
    
   
}