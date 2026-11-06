try {
    if ($Host.UI.RawUI.WindowSize.Width -lt 120) {
        $Host.UI.RawUI.BufferSize = New-Object System.Management.Automation.Host.Size(120, 100)
        $Host.UI.RawUI.WindowSize = New-Object System.Management.Automation.Host.Size(120, 35)
    }
} catch {}

if (!(Test-Path "out")) {
    New-Item -ItemType Directory -Path "out" | Out-Null
}

Write-Host "Compiling SkyLink-Flight Crew Scheduling System..."
javac -cp "lib\*" -d "out" Main.java DashboardWindow.java model\*.java dao\*.java service\*.java util\*.java

if ($LASTEXITCODE -ne 0) {
    Write-Host "Compilation failed."
    exit 1
}

Write-Host "Starting application..."
java -cp "out;lib\*" Main

