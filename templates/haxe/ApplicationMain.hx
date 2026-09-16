package;

import ::APP_MAIN::;
import lime.app.Application as LimeApplication;
import lime.system.System;
import lime.ui.WindowAttributes;
import openfl.display.Application as OpenFLApplication;

@:access(lime.app.Application)
@:access(lime.system.System)
@:dox(hide)
class ApplicationMain
{
	public static function main():Void
	{
		System.__registerEntryPoint("::APP_FILE::", create);

		#if (!html5 || munit)
		create(null);
		#end
	}

	public static function create(config:Dynamic):Void
	{
		#if !disable_preloader_assets
		ManifestResources.init(config);
		#end

		#if !munit
		var app = new OpenFLApplication();
		
		setupMetadata(app);
		setupWindows(app, config);
		setupPreloader(app);

		app.preloader.onComplete.add(function() {
			app.window.stage.addChild(new ::APP_MAIN::());
		});

		app.preloader.load();
		start(app);
		
		#else
		start(null);
		#end
	}

	private static inline function setupMetadata(app:OpenFLApplication):Void
	{
		app.meta.set("build", "::meta.buildNumber::");
		app.meta.set("company", "::meta.company::");
		app.meta.set("file", "::APP_FILE::");
		app.meta.set("name", "::meta.title::");
		app.meta.set("packageName", "::meta.packageName::");
		app.meta.set("version", "::meta.version::");
	}

	private static function setupWindows(app:OpenFLApplication, config:Dynamic):Void
	{
		#if !flash
		::foreach windows::
		var attributes:WindowAttributes = {
			allowHighDPI: ::allowHighDPI::,
			alwaysOnTop: ::alwaysOnTop::,
			borderless: ::borderless::,
			element: null,
			frameRate: ::fps::,
			#if !web fullscreen: ::fullscreen::, #end
			height: ::height::,
			hidden: #if munit true #else ::hidden:: #end,
			maximized: ::maximized::,
			minimized: ::minimized::,
			parameters: ::parameters::,
			resizable: ::resizable::,
			title: "::title::",
			width: ::width::,
			x: ::x::,
			y: ::y::,
			context: {
				antialiasing: ::antialiasing::,
				background: ::background::,
				colorDepth: ::colorDepth::,
				depth: ::depthBuffer::,
				hardware: ::hardware::,
				stencil: ::stencilBuffer::,
				type: null,
				vsync: ::vsync::
			}
		};

		if (app.window == null)
		{
			if (config != null)
			{
				for (field in Reflect.fields(config))
				{
					if (Reflect.hasField(attributes, field)) {
						Reflect.setField(attributes, field, Reflect.field(config, field));
					}
					else if (Reflect.hasField(attributes.context, field)) {
						Reflect.setField(attributes.context, field, Reflect.field(config, field));
					}
				}
			}

			#if sys
			System.__parseArguments(attributes);
			#end
		}

		app.createWindow(attributes);
		::end::
		
		#elseif air
		app.window.title = "::meta.title::";
		#else
		app.window.context.attributes.background = ::WIN_BACKGROUND::;
		app.window.frameRate = ::WIN_FPS::;
		#end
	}

	private static inline function setupPreloader(app:OpenFLApplication):Void
	{
		#if !disable_preloader_assets
		for (library in ManifestResources.preloadLibraries) {
			app.preloader.addLibrary(library);
		}

		for (name in ManifestResources.preloadLibraryNames) {
			app.preloader.addLibraryName(name);
		}
		#end
	}

	public static function start(app:LimeApplication = null):Void
	{
		#if !munit
		var result = app.exec();

		#if (sys && !ios && !nodejs && !webassembly)
		System.exit(result);
		#end

		#else
		new ::APP_MAIN::();
		#end
	}

	@:noCompletion @:dox(hide) 
	public static function __init__()
	{
		var init = LimeApplication;

		#if neko
		var sysProgramPath = {
			var moduleName = neko.vm.Module.local().name;
			try {
				sys.FileSystem.fullPath(moduleName);
			} catch (e:Dynamic) {
				if (!StringTools.endsWith(moduleName, ".n")) {
					try {
						sys.FileSystem.fullPath(moduleName + ".n");
					} catch (e:Dynamic) {
						moduleName;
					}
				} else {
					moduleName;
				}
			}
		};

		var loader = new neko.vm.Loader(untyped $loader);
		var basePath = haxe.io.Path.directory(#if (haxe_ver >= 3.3) sysProgramPath #else Sys.executablePath() #end);
		
		loader.addPath(basePath);
		loader.addPath("./");
		loader.addPath("@executable_path/");
		#end
	}
}