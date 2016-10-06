#!/usr/bin/env bash
usage() {
cat << EOF
                              
     __     __  __  __      __
 /\ |__)   |  \|__)/  \|\ ||_ 
/--\| \ .  |__/| \ \__/| \||__

              Open Wifi support

This script connects the AR Drone (Parrot Jumper Sumo) to a Open Wifi network.

Usage: script/connectOpenWifi "<essid>" -a <address> -s <subnet> -d <droneip>
  <essid>         Name of the open network to connect the drone to.
  <address>       Address to be set on the drone when connected to the network.
                    Use a different address than the router's default.
  <subnet>       Subnet mask to be set on the drone when connected to the network.
                    Example 255.255.255.0
  <droneip>       Current drone's ip address.
                    Default is 192.168.2.1
EOF
}

ESSID=$1
ADDRESS=$3
SUBNET=$5
DRONEIP=${7:-"192.168.2.1"}

if [[ -z $ADDRESS ]] && [[ -z $SUBNET ]] && [[ -z $ESSID ]]; then
  echo "ESSID: $ESSID"
  echo "ADDRESS: $ADDRESS"
  echo "SUBNET: $SUBNET"
  echo "DRONE_IP: $DRONEIP"

  # iwconfig wifi_bcm mode managed essid Nyo && ifconfig wifi_bcm 192.168.1.88 netmask 255.255.255.0 up
  # killall udhcpd && ifconfig wifi_bcm down && iwconfig wifi_bcm mode managed essid AndroidAP && ifconfig wifi_bcm up && udhcpc -b -i wifi_bcm

  { echo "killall udhcpd"; echo "iwconfig wifi_bcm mode managed essid $ESSID"; echo "ifconfig wifi_bcm $ADDRESS netmask $NETMASK up"; } | telnet $DRONEIP

  wait 5
  echo "Try to connect to new Drone address..."
  telnet $ADDRESS
fi

