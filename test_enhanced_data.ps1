# Test script to demonstrate enhanced ferry booking data
Write-Host "Testing Enhanced Ferry Booking System Data" -ForegroundColor Green
Write-Host "==========================================" -ForegroundColor Green

# Wait for server to be ready
Start-Sleep -Seconds 2

Write-Host "`n1. Testing Enhanced Ports Data:" -ForegroundColor Yellow
try {
    $portsResponse = Invoke-WebRequest -Uri "http://localhost:8080/api/ports" -UseBasicParsing
    Write-Host $portsResponse.Content
} catch {
    Write-Host "Error testing ports: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`n2. Testing Ferry Search (Port Ellen to Oban Bay at 06:00):" -ForegroundColor Yellow
try {
    $searchResponse = Invoke-WebRequest -Uri "http://localhost:8080/api/search?origin=1&dest=2&time=06:00" -UseBasicParsing
    Write-Host $searchResponse.Content
} catch {
    Write-Host "Error testing search: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`n3. Testing Ferry Search (Oban Bay to Mallaig Harbor at 10:30):" -ForegroundColor Yellow
try {
    $searchResponse2 = Invoke-WebRequest -Uri "http://localhost:8080/api/search?origin=2&dest=3&time=10:30" -UseBasicParsing
    Write-Host $searchResponse2.Content
} catch {
    Write-Host "Error testing second search: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`n4. Testing Booking (Journey ID 10, 4 passengers):" -ForegroundColor Yellow
try {
    $bookResponse = Invoke-WebRequest -Uri "http://localhost:8080/api/book?journey=10&passengers=4" -UseBasicParsing
    Write-Host $bookResponse.Content
} catch {
    Write-Host "Error testing booking: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`n5. Testing List Bookings:" -ForegroundColor Yellow
try {
    $bookingsResponse = Invoke-WebRequest -Uri "http://localhost:8080/api/bookings" -UseBasicParsing
    Write-Host $bookingsResponse.Content
} catch {
    Write-Host "Error testing bookings list: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`nTest completed!" -ForegroundColor Green