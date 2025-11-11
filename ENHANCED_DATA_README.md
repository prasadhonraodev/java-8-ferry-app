# Ferry Booking System - Enhanced Data Documentation

## Overview
This document describes the enhancements made to the ferry booking system data to provide a more realistic and comprehensive booking experience for the frontend application.

## Enhanced Data Files

### 1. Enhanced Ports (`src/main/resources/ports.txt`)
**Previous**: Basic ports with just ID and Name
**Enhanced**: Now includes:
- **Location**: Geographic location of each port
- **Code**: Three-letter port code for easy identification
- **Facilities**: Available amenities at each port

```json
{
  "Id": 1,
  "Name": "Port Ellen",
  "Location": "Isle of Islay, Scotland", 
  "Code": "PTE",
  "Facilities": "Restaurant, Shop, Parking"
}
```

**Available Ports:**
1. **Port Ellen** (PTE) - Isle of Islay, Scotland
2. **Oban Bay** (OBN) - Oban, Scotland  
3. **Mallaig Harbor** (MLG) - Mallaig, Highlands
4. **Tobermory Port** (TOB) - Isle of Mull
5. **Craignure Ferry** (CRG) - Isle of Mull
6. **Brodick Terminal** (BRD) - Isle of Arran

### 2. Enhanced Ferries (`src/main/resources/ferries.txt`)
**Previous**: Fantasy ferry names with basic capacity
**Enhanced**: Realistic Scottish ferry names with comprehensive specifications:

- **Realistic Names**: MV Caledonian Isles, MV Isle of Arran, etc.
- **Enhanced Capacity**: More realistic passenger and vehicle capacities
- **Ferry Types**: Small, Medium, and Large ferries
- **Speed Information**: Added speed specifications

```json
{
  "Id": 1,
  "Name": "MV Caledonian Isles",
  "Passengers": 120,
  "Vehicles": 35,
  "Weight": 50,
  "HomePortId": 1,
  "Type": "Large Ferry",
  "Speed": 18
}
```

**Fleet Overview:**
- **12 Total Ferries** (increased from 10)
- **Passenger Capacity**: 65-200 passengers
- **Vehicle Capacity**: 18-50 vehicles
- **Ferry Types**: Small, Medium, Large ferries
- **Realistic Names**: Based on actual Scottish ferry naming conventions

### 3. Comprehensive Timetable (`src/main/resources/timetable.txt`)
**Previous**: Limited routes between 3 ports
**Enhanced**: Extensive network covering all 6 ports with realistic schedules

**Key Improvements:**
- **49 Total Routes** (increased from 20)
- **All Ports Connected**: Every port now has multiple departures
- **Realistic Journey Times**: 45-180 minutes depending on distance
- **Multiple Daily Sailings**: 3-6 departures per route per day
- **Peak Hours Coverage**: Early morning to evening departures

**Sample Routes:**
- Port Ellen ↔ Oban Bay: 5 daily sailings, 75 minutes
- Oban Bay ↔ Mallaig Harbor: 4 daily sailings, 120 minutes  
- Tobermory Port ↔ Craignure Ferry: 6 daily sailings, 45 minutes
- And many more inter-island connections

## Frontend Booking Capabilities

### Enhanced Search Functionality
With the improved data, the frontend can now offer:

1. **Rich Port Information**: Display location, facilities, and codes
2. **Ferry Details**: Show ferry types, capacities, and specifications  
3. **Multiple Route Options**: Present various sailing times and connections
4. **Journey Planning**: Enable multi-leg journeys between any ports

### Booking Features
The enhanced data enables:

1. **Real-time Availability**: Check seat/vehicle availability on specific sailings
2. **Route Selection**: Choose from multiple ferries and times
3. **Capacity Management**: Track remaining spaces for passengers and vehicles
4. **Journey Information**: Display complete trip details including times and ferries

### Web Interface
The system provides a user-friendly web interface at `http://localhost:8080` with:

- **Search Form**: Find ferries by origin, destination, and time
- **Booking Form**: Reserve spaces with journey ID and passenger count  
- **Port List**: View all available ports and their details
- **Booking Management**: List and track current bookings

## API Endpoints

The system exposes REST API endpoints for frontend integration:

- `GET /api/ports` - List all ports with details
- `GET /api/search?origin={id}&dest={id}&time={HH:MM}` - Search available ferries
- `GET /api/book?journey={id}&passengers={count}` - Make a booking
- `GET /api/bookings` - List current bookings

## Usage Examples

### Search for Ferry Crossings
```
GET /api/search?origin=1&dest=2&time=06:00
```
Returns available ferries from Port Ellen to Oban Bay departing after 6:00 AM.

### Make a Booking
```  
GET /api/book?journey=10&passengers=4
```
Books 4 passengers on journey ID 10.

### List All Ports
```
GET /api/ports
```
Returns comprehensive port information including facilities and locations.

## Testing the Enhanced System

### Console Version
Run `./gradlew run` to see the enhanced timetable display with:
- Realistic ferry names (MV Caledonian Isles, MV Isle of Arran, etc.)
- Proper journey times and schedules
- Multiple ports and connections

### Web Version  
Run `./gradlew runWeb` and visit `http://localhost:8080` to test:
- Interactive ferry search
- Real-time booking capability
- Port information display
- Journey management

## Benefits for Frontend Development

1. **Realistic Data**: More believable ferry names and schedules
2. **Rich Information**: Additional port and ferry details for enhanced UI
3. **Scalability**: More routes and ferries for comprehensive testing
4. **User Experience**: Better journey planning with multiple options
5. **Real-world Simulation**: Scottish ferry network provides authentic context

## Future Enhancements

The current data structure supports future additions such as:
- Pricing information per route
- Seasonal schedule variations  
- Ferry maintenance schedules
- Weather-related delays
- Advanced booking features
- Multi-leg journey planning

This enhanced dataset provides a solid foundation for developing a comprehensive ferry booking frontend application with realistic Scottish maritime context.