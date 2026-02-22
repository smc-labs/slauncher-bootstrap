#if Architecture == "x64"
  #define ArchAllowed "x64compatible"
#elif Architecture == "arm64"
  #define ArchAllowed "arm64"
#else
  #error Unknown architecture
#endif

[Setup]
AppId={{C31A89FA-833D-4852-93EB-6DC968315626}
AppName={#AppName}
AppVersion={#AppVersion}
AppPublisher={#AppPublisher}
AppPublisherURL={#AppPublisherURL}
AppSupportURL={#AppPublisherURL}
AppUpdatesURL={#AppPublisherURL}
DefaultDirName={autopf}\{#AppName}

PrivilegesRequired=admin
SetupIconFile={#ResourcesDir}\icon.ico

UsePreviousAppDir=yes
DisableProgramGroupPage=yes
DisableWelcomePage=no
DisableDirPage=no

WizardStyle=modern dynamic
WizardImageAlphaFormat=defined
WizardImageFile={#ResourcesDir}\wizard-welcome\250-light.bmp
WizardImageFileDynamicDark={#ResourcesDir}\wizard-welcome\250-dark.bmp
WizardSmallImageFile={#ResourcesDir}\wizard-small\100.bmp,{#ResourcesDir}\wizard-small\125.bmp,{#ResourcesDir}\wizard-small\150.bmp,{#ResourcesDir}\wizard-small\175.bmp,{#ResourcesDir}\wizard-small\200.bmp,{#ResourcesDir}\wizard-small\225.bmp,{#ResourcesDir}\wizard-small\250.bmp
WizardSmallImageFileDynamicDark={#ResourcesDir}\wizard-small\100.bmp,{#ResourcesDir}\wizard-small\125.bmp,{#ResourcesDir}\wizard-small\150.bmp,{#ResourcesDir}\wizard-small\175.bmp,{#ResourcesDir}\wizard-small\200.bmp,{#ResourcesDir}\wizard-small\225.bmp,{#ResourcesDir}\wizard-small\250.bmp

Compression=lzma
SolidCompression=yes

ArchitecturesAllowed={#ArchAllowed}
ArchitecturesInstallIn64BitMode={#ArchAllowed}

OutputDir={#OutputDir}
OutputBaseFilename=simpleminecraft-installer-{#AppVersion}-{#Architecture}

[Languages]
Name: "english"; MessagesFile: "compiler:Default.isl,{#ResourcesDir}/lang-en.isl"
Name: "russian"; MessagesFile: "compiler:Languages\Russian.isl,{#ResourcesDir}/lang-ru.isl"

[Tasks]
Name: "desktopicon"; Description: "{cm:CreateDesktopIcon}"; GroupDescription: "{cm:AdditionalIcons}"; Flags: unchecked

[Files]
Source: "{#BuiltImageDir}\*"; DestDir: "{app}"; Flags: ignoreversion recursesubdirs createallsubdirs

[Icons]
Name: "{autoprograms}\{#AppName}"; Filename: "{app}\{#EntryPoint}"
Name: "{autodesktop}\{#AppName}"; Filename: "{app}\{#EntryPoint}"; Tasks: desktopicon

[Run]
Filename: "{app}\{#EntryPoint}"; Description: "{cm:LaunchProgram,{#StringChange(AppName, '&', '&&')}}"; Flags: nowait postinstall skipifsilent
