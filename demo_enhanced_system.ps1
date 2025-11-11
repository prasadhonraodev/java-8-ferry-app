# Ferry Booking System Demo Script
# This script demonstrates the enhanced data and booking capabilities

Write-Host "🚢 Enhanced Ferry Booking System Demo" -ForegroundColor Cyan
Write-Host "====================================" -ForegroundColor Cyan

Write-Host "`n📋 Enhanced Data Summary:" -ForegroundColor Yellow
Write-Host "• 6 Realistic Scottish Ports (up from 3)" -ForegroundColor Green
Write-Host "• 12 Ferries with Scottish Names (up from 10)" -ForegroundColor Green  
Write-Host "• 49+ Ferry Routes (up from 20)" -ForegroundColor Green
Write-Host "• Realistic Journey Times & Schedules" -ForegroundColor Green
Write-Host "• Port Facilities & Location Information" -ForegroundColor Green

Write-Host "`n🏗️ Available Ports:" -ForegroundColor Yellow
@(
    "1. Port Ellen (PTE) - Isle of Islay, Scotland",
    "2. Oban Bay (OBN) - Oban, Scotland", 
    "3. Mallaig Harbor (MLG) - Mallaig, Highlands",
    "4. Tobermory Port (TOB) - Isle of Mull",
    "5. Craignure Ferry (CRG) - Isle of Mull", 
    "6. Brodick Terminal (BRD) - Isle of Arran"
) | ForEach-Object { Write-Host "   $_" -ForegroundColor White }

Write-Host "`n🚢 Sample Enhanced Ferries:" -ForegroundColor Yellow
@(
    "• MV Caledonian Isles (120 pax, 35 vehicles)",
    "• MV Isle of Arran (90 pax, 28 vehicles)",
    "• MV Hebridean Princess (180 pax, 45 vehicles)",
    "• MV Sound of Mull (200 pax, 50 vehicles)",
    "• MV Loch Fyne (75 pax, 22 vehicles)"
) | ForEach-Object { Write-Host "   $_" -ForegroundColor White }

Write-Host "`n🗓️ Sample Routes Available:" -ForegroundColor Yellow
@(
    "• Port Ellen ↔ Oban Bay: 5 daily sailings (75 min)",
    "• Oban Bay ↔ Mallaig Harbor: 4 daily sailings (120 min)",
    "• Tobermory ↔ Craignure: 6 daily sailings (45 min)",
    "• Oban Bay ↔ Brodick Terminal: 4 daily sailings (105 min)",
    "• Port Ellen ↔ Brodick Terminal: 2 daily sailings (180 min)"
) | ForEach-Object { Write-Host "   $_" -ForegroundColor White }

Write-Host "`n🌐 Web Interface Features:" -ForegroundColor Yellow
@(
    "• Interactive ferry search by origin/destination/time",
    "• Real-time booking with passenger count",
    "• Comprehensive port information display", 
    "• Journey management and booking history",
    "• RESTful API endpoints for frontend integration"
) | ForEach-Object { Write-Host "   $_" -ForegroundColor White }

Write-Host "`n🚀 To Start Testing:" -ForegroundColor Yellow
Write-Host "   1. Console: ./gradlew run" -ForegroundColor White
Write-Host "   2. Web UI: ./gradlew runWeb then visit http://localhost:8080" -ForegroundColor White
Write-Host "   3. API: Use endpoints like /api/ports, /api/search, /api/book" -ForegroundColor White

Write-Host "`n📚 For Full Details:" -ForegroundColor Yellow
Write-Host "   See: ENHANCED_DATA_README.md" -ForegroundColor White

Write-Host "`n✅ Enhancement Complete!" -ForegroundColor Green
Write-Host "The ferry booking system now has comprehensive, realistic data" -ForegroundColor Green  
Write-Host "suitable for frontend development and user testing." -ForegroundColor Green

# Show current server status
if (Get-Process -Name "java" -ErrorAction SilentlyContinue) {
    Write-Host "`n🟢 Web server appears to be running at http://localhost:8080" -ForegroundColor Green
} else {
    Write-Host "`n⚪ Web server not currently running" -ForegroundColor Gray
    Write-Host "   Run './gradlew runWeb' to start it" -ForegroundColor Gray
}