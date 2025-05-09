﻿#include "Shlwapi.h"
#include "framework.h"
#include <codecvt>
#include <locale>
#include <string.h>
#include <strsafe.h>
#include <tlhelp32.h>
#include <vector>
#include <wchar.h>
#include <fstream>
#include <sstream>

#include "log.h"
#include "util.h"
#include "spy_types.h"

#pragma comment(lib, "shlwapi")
#pragma comment(lib, "Version.lib")

using namespace std;

wstring String2Wstring(string s)
{
    if (s.empty())
        return wstring();
    int size_needed = MultiByteToWideChar(CP_UTF8, 0, &s[0], (int)s.size(), NULL, 0);
    wstring ws(size_needed, 0);
    MultiByteToWideChar(CP_UTF8, 0, &s[0], (int)s.size(), &ws[0], size_needed);
    return ws;
}

string Wstring2String(wstring ws)
{
    if (ws.empty())
        return string();
    int size_needed = WideCharToMultiByte(CP_UTF8, 0, &ws[0], (int)ws.size(), NULL, 0, NULL, NULL);
    string s(size_needed, 0);
    WideCharToMultiByte(CP_UTF8, 0, &ws[0], (int)ws.size(), &s[0], size_needed, NULL, NULL);
    return s;
}

string GB2312ToUtf8(const char *gb2312)
{
    int size_needed = 0;

    size_needed = MultiByteToWideChar(CP_ACP, 0, gb2312, -1, NULL, 0);
    wstring ws(size_needed, 0);
    MultiByteToWideChar(CP_ACP, 0, gb2312, -1, &ws[0], size_needed);

    size_needed = WideCharToMultiByte(CP_UTF8, 0, &ws[0], -1, NULL, 0, NULL, NULL);
    string s(size_needed, 0);
    WideCharToMultiByte(CP_UTF8, 0, &ws[0], -1, &s[0], size_needed, NULL, NULL);

    return s;
}
/*
 * https://blog.csdn.net/qq_19875391/article/details/136057578
 */
bool isGB2312(const std::string& strIn)
{
    unsigned int nBytes = 0;
    unsigned char chr = strIn.at(0);
    bool bAllAscii = true; 
    for (unsigned int i = 0; strIn[i] != '\0'; ++i) {
        chr = strIn.at(i);
        if ((chr & 0x80) != 0 && nBytes == 0) {
            bAllAscii = false;
        }
        if (nBytes == 0) {
            if (chr >= 0x80) {
                if (chr >= 0x81 && chr <= 0xFE) {
                    nBytes = +2;
                } else {
                    return false;
                }
                nBytes--;
            }
        }
        else {
            if (chr < 0x40 || chr>0xFE) {
                return false;
            }
            nBytes--;
        }
    }
    if (nBytes != 0) return false;
    if (bAllAscii) return true;
    return true;
}
std::map<std::string, std::string> read_properties(const std::string file_path) {
    std::map<std::string, std::string> properties;
    std::ifstream file(file_path);
    std::string line;

    if (file.is_open()) {
        while (std::getline(file, line)) {
            std::istringstream is_line(line);
            std::string key;
            if (std::getline(is_line, key, '=')) {
                std::string value;
                if (std::getline(is_line, value)) {
                    properties[key] = value;
                }
            }
        }
        file.close();
    }

    return properties;
}
static int GetWeChatPath0(wchar_t* path, HKEY parent, string parentStr, WCHAR *prefix)
{
    int ret = -1;
    HKEY hKey = NULL;
    WCHAR p1[MAX_PATH] = L"";
    DWORD Type = REG_SZ;
    DWORD cbData = MAX_PATH * sizeof(WCHAR);
    if (prefix != NULL)
    {
        lstrcpy(p1, prefix);
        lstrcat(p1, L"\\");
    }
    lstrcat(p1, L"Software\\Tencent\\WeChat");
    // HKEY_CURRENT_USER\Software\Tencent\WeChat InstallPath = xx
    if (ERROR_SUCCESS == RegOpenKey(parent, p1, &hKey))
    {
        if (ERROR_SUCCESS == RegQueryValueEx(hKey, L"InstallPath", 0, &Type, (LPBYTE)path, &cbData))
        {
            ret = ERROR_SUCCESS;
            goto __exit;
        }
    }
    if (hKey) RegCloseKey(hKey);
    
    if (prefix != NULL)
    {
        lstrcpy(p1, prefix);
        lstrcat(p1, L"\\");
    }
    lstrcat(p1, L"Software\\Tencent\\bugReport\\WechatWindows");

    // HKEY_CURRENT_USER\Software\Tencent\bugReport\WechatWindows InstallDir = xx
    if (ERROR_SUCCESS == RegOpenKey(parent, p1, &hKey))
    {
        if (ERROR_SUCCESS == RegQueryValueEx(hKey, L"InstallDir", 0, &Type, (LPBYTE)path, &cbData))
        {
            if (path) PathRemoveFileSpec(path);
            ret = ERROR_SUCCESS;
            goto __exit;
        }
    }
    if (prefix == NULL) LOG_WARN("Failed to get WeChat path from Registry ({})", parentStr);
    ret = GetLastError();
__exit:
    if (ret == ERROR_SUCCESS && path != NULL) {
        PathAppend(path, WECHAREXE);
    }
    if (hKey) RegCloseKey(hKey);
    
    return ret;
}

static int GetWeChatPath(wchar_t* path)
{
    int ret = -1;
    HKEY hUsers = NULL;
    DWORD dwIndex = 0;
    WCHAR szName[MAX_PATH] = L"";
    WCHAR name[MAX_PATH] = L"";
    DWORD cchName = MAX_PATH;

    if (ERROR_SUCCESS != RegOpenKey(HKEY_USERS, L"", &hUsers)) {
        LOG_WARN("Failed to read from Registry (HKEY_USERS)，Trying old methods to get WeChat path.");
        return GetWeChatPath0(path, HKEY_CURRENT_USER, "HKEY_CURRENT_USER", NULL);
    }
    if (hUsers)
    {
        while (ERROR_SUCCESS == RegEnumKeyEx(hUsers, dwIndex++, szName, &cchName, NULL, NULL, NULL, NULL))
        {
            cchName = MAX_PATH;
            ret = GetWeChatPath0(path, HKEY_USERS, "HKEY_USERS", szName);
            if (ERROR_SUCCESS == ret) break;
        }
        RegCloseKey(hUsers);
    }

    return ret;
}

static int GetWeChatWinDLLPath(wchar_t *path)
{
    int ret = GetWeChatPath(path);
    if (ret != ERROR_SUCCESS) {
        return ret;
    }

    PathRemoveFileSpecW(path);
    PathAppendW(path, WECHATWINDLL);
    if (!PathFileExists(path)) {
        // 微信从（大约）3.7开始，增加了一层版本目录: [3.7.0.29]
        PathRemoveFileSpec(path);
        _wfinddata_t findData;
        wstring dir     = wstring(path) + L"\\[*.*";
        intptr_t handle = _wfindfirst(dir.c_str(), &findData);
        if (handle == -1) { // 检查是否成功
            return -1;
        }
        wstring dllPath = wstring(path) + L"\\" + findData.name;
        wcscpy_s(path, MAX_PATH, dllPath.c_str());
        PathAppend(path, WECHATWINDLL);
    }

    return ret;
}

static bool GetFileVersion(const wchar_t *filePath, wchar_t *version)
{
    if (wcslen(filePath) > 0 && PathFileExists(filePath)) {
        VS_FIXEDFILEINFO *pVerInfo = NULL;
        DWORD dwTemp, dwSize;
        BYTE *pData = NULL;
        UINT uLen;

        dwSize = GetFileVersionInfoSize(filePath, &dwTemp);
        if (dwSize == 0) {
            return false;
        }

        pData = new BYTE[dwSize + 1];
        if (pData == NULL) {
            return false;
        }

        if (!GetFileVersionInfo(filePath, 0, dwSize, pData)) {
            delete[] pData;
            return false;
        }

        if (!VerQueryValue(pData, TEXT("\\"), (void **)&pVerInfo, &uLen)) {
            delete[] pData;
            return false;
        }

        UINT64 verMS    = pVerInfo->dwFileVersionMS;
        UINT64 verLS    = pVerInfo->dwFileVersionLS;
        UINT64 major    = HIWORD(verMS);
        UINT64 minor    = LOWORD(verMS);
        UINT64 build    = HIWORD(verLS);
        UINT64 revision = LOWORD(verLS);
        delete[] pData;

        StringCbPrintf(version, 0x20, TEXT("%d.%d.%d.%d"), major, minor, build, revision);

        return true;
    }

    return false;
}

int GetWeChatVersion(wchar_t *version)
{
    WCHAR Path[MAX_PATH] = { 0 };

    int ret = GetWeChatWinDLLPath(Path);
    if (ret != ERROR_SUCCESS) {
        return ret;
    }

    ret = GetFileVersion(Path, version);

    return ret;
}

DWORD GetWeChatPid()
{
    DWORD pid           = 0;
    HANDLE hSnapshot    = CreateToolhelp32Snapshot(TH32CS_SNAPPROCESS, 0);
    PROCESSENTRY32 pe32 = { sizeof(PROCESSENTRY32) };
    while (Process32Next(hSnapshot, &pe32)) {
        wstring strProcess = pe32.szExeFile;
        if (strProcess == WECHAREXE) {
            pid = pe32.th32ProcessID;
            break;
        }
    }
    CloseHandle(hSnapshot);
    return pid;
}

enum class WindowsArchiture { x32, x64 };
static WindowsArchiture GetWindowsArchitecture()
{
#ifdef _WIN64
    return WindowsArchiture::x64;
#else
    return WindowsArchiture::x32;
#endif
}

BOOL IsProcessX64(DWORD pid)
{
    BOOL isWow64    = false;
    HANDLE hProcess = OpenProcess(PROCESS_QUERY_LIMITED_INFORMATION, false, pid);
    if (!hProcess)
        return false;
    BOOL result = IsWow64Process(hProcess, &isWow64);
    CloseHandle(hProcess);
    if (!result)
        return false;
    if (isWow64)
        return false;
    else if (GetWindowsArchitecture() == WindowsArchiture::x32)
        return false;
    else
        return true;
}

int OpenWeChat(DWORD *pid, bool *firstOpen, bool startNew)
{
    if (!startNew) {
        *pid = GetWeChatPid();
        if (*pid) {
            *firstOpen = false;
            LOG_INFO("WeChat has started, re-using process");
            return ERROR_SUCCESS;
        }
    }
    *firstOpen = true;

    int ret                = -1;
    STARTUPINFO si         = { sizeof(si) };
    WCHAR Path[MAX_PATH]   = { 0 };
    PROCESS_INFORMATION pi = { 0 };

    ret = GetWeChatPath(Path);
    if (ERROR_SUCCESS != ret) {
        return ret;
    }
    if (startNew) {
        LOG_INFO("Starting WeChat from: {}", Wstring2String(Path));
    } else {
        LOG_INFO("WeChat not started, Starting from: {}", Wstring2String(Path));
    }

    if (!CreateProcess(NULL, Path, NULL, NULL, FALSE, CREATE_NEW_CONSOLE, NULL, NULL, &si, &pi)) {
        return GetLastError();
    }

    CloseHandle(pi.hThread);
    CloseHandle(pi.hProcess);

    *pid = pi.dwProcessId;

    return ERROR_SUCCESS;
}

size_t GetWstringByAddress(UINT64 addr, wchar_t *buffer, UINT64 buffer_size)
{
    size_t strLength = GET_DWORD(addr + 8);
    if (strLength == 0) {
        return 0;
    } else if (strLength > buffer_size) {
        strLength = buffer_size - 1;
    }

    wmemcpy_s(buffer, strLength + 1, GET_WSTRING(addr), strLength + 1);

    return strLength;
}

string GetStringByAddress(UINT64 addr)
{
    size_t strLength = GET_DWORD(addr + 8);
    return Wstring2String(wstring(GET_WSTRING(addr), strLength));
}

string GetStringByStrAddr(UINT64 addr)
{
    size_t strLength = GET_DWORD(addr + 8);
    return strLength ? string(GET_STRING(addr), strLength) : string();
}

string GetStringByWstrAddr(UINT64 addr)
{
    size_t strLength = GET_DWORD(addr + 8);
    return strLength ? Wstring2String(wstring(GET_WSTRING(addr), strLength)) : string();
}

UINT32 GetMemoryIntByAddress(HANDLE hProcess, UINT64 addr)
{
    UINT32 value = 0;

    unsigned char data[4] = { 0 };
    if (ReadProcessMemory(hProcess, (LPVOID)addr, data, 4, 0)) {
        value = data[0] & 0xFF;
        value |= ((data[1] << 8) & 0xFF00);
        value |= ((data[2] << 16) & 0xFF0000);
        value |= ((data[3] << 24) & 0xFF000000);
    }

    return value;
}

wstring GetUnicodeInfoByAddress(HANDLE hProcess, UINT64 address)
{
    wstring value = L"";

    UINT64 strAddress = GetMemoryIntByAddress(hProcess, address);
    UINT64 strLen     = GetMemoryIntByAddress(hProcess, address + 0x4);
    if (strLen > 500)
        return value;

    wchar_t cValue[500] = { 0 };
    memset(cValue, 0, sizeof(cValue) / sizeof(wchar_t));
    if (ReadProcessMemory(hProcess, (LPVOID)strAddress, cValue, (strLen + 1) * 2, 0)) {
        value = wstring(cValue);
    }

    return value;
}

void DbgMsg(const char *zcFormat, ...)
{
    // initialize use of the variable argument array
    va_list vaArgs;
    va_start(vaArgs, zcFormat);

    // reliably acquire the size
    // from a copy of the variable argument array
    // and a functionally reliable call to mock the formatting
    va_list vaArgsCopy;
    va_copy(vaArgsCopy, vaArgs);
    const int iLen = std::vsnprintf(NULL, 0, zcFormat, vaArgsCopy);
    va_end(vaArgsCopy);

    // return a formatted string without risking memory mismanagement
    // and without assuming any compiler or platform specific behavior
    std::vector<char> zc(iLen + 1);
    std::vsnprintf(zc.data(), zc.size(), zcFormat, vaArgs);
    va_end(vaArgs);
    std::string strText(zc.data(), iLen);

    OutputDebugStringA(strText.c_str());
}

WxString *NewWxStringFromStr(const string &str) { return NewWxStringFromWstr(String2Wstring(str)); }

WxString *NewWxStringFromWstr(const wstring &ws)
{
    WxString *p       = (WxString *)HeapAlloc(GetProcessHeap(), 0, sizeof(WxString));
    wchar_t *pWstring = (wchar_t *)HeapAlloc(GetProcessHeap(), 0, (ws.size() + 1) * 2);
    if (p == NULL || pWstring == NULL) {
        LOG_ERROR("Out of Memory...");
        return NULL;
    }

    wmemcpy(pWstring, ws.c_str(), ws.size() + 1);
    p->wptr     = pWstring;
    p->size     = (DWORD)ws.size();
    p->capacity = (DWORD)ws.size();
    p->ptr      = 0;
    p->clen     = 0;
    return p;
}
