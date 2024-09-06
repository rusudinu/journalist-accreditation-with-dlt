import {Body, Controller, Get, Param, Post} from '@nestjs/common';
import {RegistryService} from './registry.service';
import {RegistryModel} from './registry.model';

@Controller('registry')
export class RegistryController {
    constructor(private readonly registryService: RegistryService) {

    }

    @Get('init')
    initLedger() {
        return this.registryService.initChain();
    }


    @Get('all')
    showEntireLedger() {
        return this.registryService.findAll();
    }

    @Get()
    getRegistry(): string {
        return 'hello world';
    }

    @Get(':requestId')
    getRegistryByRequestId(@Param('requestId') requestId: string) {
        return this.registryService.findById(requestId);
    }

    @Post()
    saveOrUpdateRegistry(@Body() request: RegistryModel): Promise<void> {
        console.log(request);
        return this.registryService.createOrUpdateRegistryEntry(request);
    }
}
